package com.googlesource.gerrit.plugins.rabbitmq;

import com.google.gerrit.common.Version;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.config.PluginConfig;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.inject.Inject;

import com.rabbitmq.client.AMQP;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.lib.Config;

import java.util.HashMap;
import java.util.Map;

public class Properties {

  private final static String GERRIT = "gerrit";
  private final static String CONTENT_TYPE_JSON = "application/json";

  private final static int DEFAULT_MESSAGE_DELIVERY_MODE = 1;
  private final static int DEFAULT_MESSAGE_PRIORITY = 1;
  private final static int DEFAULT_GERRIT_PORT = 29418;
  private final static String DEFAULT_GERRIT_SCHEME = "ssh";
  private final static int DEFAULT_CONNECTION_MONITOR_INTERVAL = 15000;
  private final static int MINIMUM_CONNECTION_MONITOR_INTERVAL = 5000;

  private final Config config;
  private final PluginConfig pluginConfig;
  private AMQP.BasicProperties properties;

  @Inject
  public Properties(@PluginName String pluginName,
      @GerritServerConfig final Config config, PluginConfigFactory factory) {
    this.config = config;
    this.pluginConfig = factory.getFromGerritConfig(pluginName);
  }

  public String getAMQPUri() {
    return pluginConfig.getString(Keys.AMQP_URI.property, "");
  }

  public String getAMQPUsername() {
    return pluginConfig.getString(Keys.AMQP_USERNAME.property, "");
  }

  public String getAMQPPassword() {
    return pluginConfig.getString(Keys.AMQP_PASSWORD.property, "");
  }

  public String getAMQPQueue() {
    return pluginConfig.getString(Keys.AMQP_QUEUE.property, "");
  }

  public String getAMQPExchange() {
    return pluginConfig.getString(Keys.AMQP_EXCHANGE.property, "");
  }

  public String getAMQPRoutingKey() {
    return pluginConfig.getString(Keys.AMQP_ROUTINGKEY.property, "");
  }

  public int getMessageDeliveryMode() {
    return pluginConfig.getInt(Keys.MESSAGE_DELIVERY_MODE.property, DEFAULT_MESSAGE_DELIVERY_MODE);
  }

  public int getMessagePriority() {
    return pluginConfig.getInt(Keys.MESSAGE_PRIORITY.property, DEFAULT_MESSAGE_PRIORITY);
  }

  public String getGerritName() {
    return pluginConfig.getString(Keys.GERRIT_NAME.property, "");
  }

  public String getGerritHostname() {
    return pluginConfig.getString(Keys.GERRIT_HOSTNAME.property, "");
  }

  public String getGerritScheme() {
    return pluginConfig.getString(Keys.GERRIT_SCHEME.property, DEFAULT_GERRIT_SCHEME);
  }

  public int getGerritPort() {
    return pluginConfig.getInt(Keys.GERRIT_PORT.property, DEFAULT_GERRIT_PORT);
  }

  public String getGerritFrontUrl() {
    return StringUtils.stripToEmpty(config.getString(GERRIT, null, Keys.GERRIT_FRONT_URL.property));
  }

  public String getGerritVersion() {
    return StringUtils.stripToEmpty(Version.getVersion());
  }

  public int getConnectionMonitorInterval() {
    int interval = pluginConfig.getInt(Keys.CONNECTION_MONITOR_INTERVAL.property, DEFAULT_CONNECTION_MONITOR_INTERVAL);
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
