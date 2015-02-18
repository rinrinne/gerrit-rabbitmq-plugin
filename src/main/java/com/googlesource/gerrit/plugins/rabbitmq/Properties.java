// Copyright (C) 2013 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.rabbitmq;

import com.google.gerrit.common.Version;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.config.SitePaths;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.rabbitmq.client.AMQP;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class Properties {

  // TODO: Value will be replaced to "gerrit.event".
  public final static String EVENT_APPID = "gerrit";
  public final static String APPROVE_APPID = "gerrit.approve";
  public final static String CONTENT_TYPE_JSON = "application/json";

  private static final Logger LOGGER = LoggerFactory.getLogger(Properties.class);
  private final static String CONFIG_FILENAME = "rabbitmq.config";

  private final static int MINIMUM_CONNECTION_MONITOR_INTERVAL = 5000;

  private final Config config;
  private final Config pluginConfig;
  private AMQP.BasicProperties properties;

  @Inject
  public Properties(final SitePaths site, @GerritServerConfig final Config config) {
    this.config = config;
    this.pluginConfig = getPluginConfig(new File(site.etc_dir, CONFIG_FILENAME));
    this.properties = generateBasicProperties();
  }

  public Config getPluginConfig(File cfgPath) {
    LOGGER.info("Loading " + cfgPath.toString() + " ...");
    FileBasedConfig cfg = new FileBasedConfig(cfgPath, FS.DETECTED);
    if (!cfg.getFile().exists()) {
      LOGGER.warn("No " + cfg.getFile());
      return cfg;
    }
    if (cfg.getFile().length() == 0) {
      LOGGER.info("Empty " + cfg.getFile());
      return cfg;
    }

    try {
      cfg.load();
    } catch (ConfigInvalidException e) {
      LOGGER.info("Config file " + cfg.getFile() + " is invalid: " + e.getMessage());
    } catch (IOException e) {
      LOGGER.info("Cannot read " + cfg.getFile() + ": " + e.getMessage());
    }
    return cfg;
  }

  public String getString(Keys key) {
    String val = pluginConfig.getString(key.section, null, key.name);
    if (val == null) {
      return key.defaultVal.toString();
    }
    return val;
  }

  public int getInt(Keys key) {
    return pluginConfig.getInt(key.section, key.name, new Integer(key.defaultVal.toString()));
  }

  public boolean getBoolean(Keys key) {
    return pluginConfig.getBoolean(key.section, key.name, new Boolean(key.defaultVal.toString()));
  }

  public String getGerritFrontUrl() {
    return StringUtils.stripToEmpty(config.getString(Keys.GERRIT_FRONT_URL.section, null, Keys.GERRIT_FRONT_URL.name));
  }

  public boolean hasListenAs() {
    return !getListenAs().isEmpty();
  }

  public String getListenAs() {
    return StringUtils.stripToEmpty(pluginConfig.getString(
        Keys.GERRIT_LISTENAS.section, null, Keys.GERRIT_LISTENAS.name));
  }

  public String getGerritVersion() {
    return StringUtils.stripToEmpty(Version.getVersion());
  }

  public int getConnectionMonitorInterval() {
    int interval = getInt(Keys.MONITOR_INTERVAL);
    if (interval < MINIMUM_CONNECTION_MONITOR_INTERVAL) {
      return MINIMUM_CONNECTION_MONITOR_INTERVAL;
    }
    return interval;
  }

  private AMQP.BasicProperties generateBasicProperties() {
    Map<String, Object> headers = new HashMap<>();
    headers.put(Keys.GERRIT_NAME.key, getString(Keys.GERRIT_NAME));
    headers.put(Keys.GERRIT_HOSTNAME.key, getString(Keys.GERRIT_HOSTNAME));
    headers.put(Keys.GERRIT_SCHEME.key, getString(Keys.GERRIT_SCHEME));
    headers.put(Keys.GERRIT_PORT.key, String.valueOf(getInt(Keys.GERRIT_PORT)));
    headers.put(Keys.GERRIT_FRONT_URL.key, getGerritFrontUrl());
    headers.put(Keys.GERRIT_VERSION.key, getGerritVersion());

    AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
    builder.appId(EVENT_APPID);
    builder.contentEncoding(CharEncoding.UTF_8);
    builder.contentType(CONTENT_TYPE_JSON);
    builder.deliveryMode(getInt(Keys.MESSAGE_DELIVERY_MODE));
    builder.priority(getInt(Keys.MESSAGE_PRIORITY));
    builder.headers(headers);

    return builder.build();
  }

  public AMQP.BasicProperties getBasicProperties() {
    return properties;
  }
}
