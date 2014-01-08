package com.googlesource.gerrit.plugins.rabbitmq;

import com.google.gerrit.common.Version;
import com.google.gerrit.extensions.annotations.PluginName;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(Properties.class);
  private final static String GERRIT = "gerrit";
  private final static String CONTENT_TYPE_JSON = "application/json";

  private final static int DEFAULT_MESSAGE_DELIVERY_MODE = 1;
  private final static int DEFAULT_MESSAGE_PRIORITY = 0;
  private final static int DEFAULT_GERRIT_PORT = 29418;
  private final static String DEFAULT_GERRIT_SCHEME = "ssh";
  private final static int DEFAULT_CONNECTION_MONITOR_INTERVAL = 15000;
  private final static int MINIMUM_CONNECTION_MONITOR_INTERVAL = 5000;

  private final Config config;
  private final Config pluginConfig;
  private AMQP.BasicProperties properties;

  @Inject
  public Properties(@PluginName final String pluginName, final SitePaths site,
      @GerritServerConfig final Config config) {
    this.config = config;
    this.pluginConfig = getPluginConfig(new File(site.etc_dir, pluginName + ".config"));
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

  private String getConfigString(Keys key, String defaultValue) {
    String val = pluginConfig.getString(key.section, null, key.value);
    if (val == null) {
      return defaultValue;
    } else {
      return val;
    }
  }

  private int getConfigInt(Keys key, int defaultValue) {
    return pluginConfig.getInt(key.section, key.value, defaultValue);
  }

  private boolean getConfigBoolean(Keys key, boolean defaultValue) {
    return pluginConfig.getBoolean(key.section, key.value, defaultValue);
  }

  public String getAMQPUri() {
    return getConfigString(Keys.AMQP_URI, "amqp://localhost");
  }

  public String getAMQPUsername() {
    return getConfigString(Keys.AMQP_USERNAME, "");
  }

  public String getAMQPPassword() {
    return getConfigString(Keys.AMQP_PASSWORD, "");
  }

  public String getAMQPQueue() {
    return getConfigString(Keys.AMQP_QUEUE, "");
  }

  public String getAMQPExchange() {
    return getConfigString(Keys.AMQP_EXCHANGE, "");
  }

  public String getAMQPRoutingKey() {
    return getConfigString(Keys.AMQP_ROUTINGKEY, "");
  }

  public int getMessageDeliveryMode() {
    return getConfigInt(Keys.MESSAGE_DELIVERY_MODE, DEFAULT_MESSAGE_DELIVERY_MODE);
  }

  public int getMessagePriority() {
    return getConfigInt(Keys.MESSAGE_PRIORITY, DEFAULT_MESSAGE_PRIORITY);
  }

  public String getGerritName() {
    return getConfigString(Keys.GERRIT_NAME, "");
  }

  public String getGerritHostname() {
    return getConfigString(Keys.GERRIT_HOSTNAME, "");
  }

  public String getGerritScheme() {
    return getConfigString(Keys.GERRIT_SCHEME, DEFAULT_GERRIT_SCHEME);
  }

  public int getGerritPort() {
    return getConfigInt(Keys.GERRIT_PORT, DEFAULT_GERRIT_PORT);
  }

  public String getGerritFrontUrl() {
    return StringUtils.stripToEmpty(config.getString(GERRIT, null, Keys.GERRIT_FRONT_URL.value));
  }

  public String getGerritVersion() {
    return StringUtils.stripToEmpty(Version.getVersion());
  }

  public int getConnectionMonitorInterval() {
    int interval = getConfigInt(Keys.CONNECTION_MONITOR_INTERVAL, DEFAULT_CONNECTION_MONITOR_INTERVAL);
    if (interval < MINIMUM_CONNECTION_MONITOR_INTERVAL) {
      return MINIMUM_CONNECTION_MONITOR_INTERVAL;
    }
    return interval;
  }

  public AMQP.BasicProperties getBasicProperties() {
    if (properties == null) {
      Map<String, Object> headers = new HashMap<String, Object>();
      headers.put(Keys.GERRIT_NAME.header, getGerritName());
      headers.put(Keys.GERRIT_HOSTNAME.header, getGerritHostname());
      headers.put(Keys.GERRIT_SCHEME.header, getGerritScheme());
      headers.put(Keys.GERRIT_PORT.header, String.valueOf(getGerritPort()));
      headers.put(Keys.GERRIT_FRONT_URL.header, getGerritFrontUrl());
      headers.put(Keys.GERRIT_VERSION.header, getGerritVersion());

      AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
      builder.appId(GERRIT);
      builder.contentEncoding(CharEncoding.UTF_8);
      builder.contentType(CONTENT_TYPE_JSON);
      builder.deliveryMode(getMessageDeliveryMode());
      builder.priority(getMessagePriority());
      builder.headers(headers);

      properties = builder.build();
    }
    return properties;
  }
}
