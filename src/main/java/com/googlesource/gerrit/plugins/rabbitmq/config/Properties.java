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

package com.googlesource.gerrit.plugins.rabbitmq.config;

import com.google.gerrit.common.Version;

import com.googlesource.gerrit.plugins.rabbitmq.Keys;
import com.googlesource.gerrit.plugins.rabbitmq.config.internal.GerritFrontUrl;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Properties implements GerritFrontUrl {

  private static final Logger LOGGER = LoggerFactory.getLogger(Properties.class);

  private final static int MINIMUM_CONNECTION_MONITOR_INTERVAL = 5000;

  private final Path propertiesFile;
  private Config pluginConfig;
  private AMQProperties amqProperties;

  public Properties(final Path propertiesFile) {
    this.propertiesFile = propertiesFile;
  }

  public boolean load() {
    return load(null);
  }

  public boolean load(final Properties baseProperties) {
    pluginConfig = new Config();
    LOGGER.info("Loading {} ...", propertiesFile);
    if (!Files.exists(propertiesFile)) {
      LOGGER.warn("No {}", propertiesFile);
      return false;
    }

    FileBasedConfig cfg;
    try {
      if (baseProperties != null) {
        cfg = new FileBasedConfig(baseProperties.getConfig(), propertiesFile.toFile(), FS.DETECTED);
      } else {
        cfg = new FileBasedConfig(propertiesFile.toFile(), FS.DETECTED);
      }
      cfg.load();
    } catch (ConfigInvalidException e) {
      LOGGER.info("{} has invalid format: {}", propertiesFile, e.getMessage());
      return false;
    } catch (IOException e) {
      LOGGER.info("Cannot read {}: {}", propertiesFile, e.getMessage());
      return false;
    }
    pluginConfig = cfg;
    return true;
  }

  public Config getConfig() {
    return pluginConfig;
  }

  public Path getPath() {
    return propertiesFile;
  }

  public String getName() {
    return FilenameUtils.removeExtension(propertiesFile.getFileName().toString());
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
    return StringUtils.stripToEmpty(pluginConfig.getString(
        Keys.GERRIT_FRONT_URL.section, null, Keys.GERRIT_FRONT_URL.name));
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

  public AMQProperties getAMQProperties() {
    if (amqProperties == null) {
      amqProperties = new AMQProperties(this);
    }
    return amqProperties;
  }

  @Override
  public void setGerritFrontUrlFromConfig(Config config) {
    if (pluginConfig != null) {
      pluginConfig.setString(
        Keys.GERRIT_FRONT_URL.section, null, Keys.GERRIT_FRONT_URL.name,
        config.getString(Keys.GERRIT_FRONT_URL.section, null, Keys.GERRIT_FRONT_URL.name));
    }
  }
}
