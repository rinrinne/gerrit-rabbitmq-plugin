package com.googlesource.gerrit.plugins.rabbitmq;

import com.google.gerrit.common.Version;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.config.PluginConfig;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.rabbitmq.client.AMQP;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.lib.Config;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class Properties {

  private final static String GERRIT = "gerrit";
  private final static String CONTENT_TYPE_JSON = "application/json";

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
    return StringUtils.stripToEmpty(pluginConfig.getString(Keys.AMQP_URI.property));
  }

  public String getAMQPUsername() {
    return StringUtils.stripToEmpty(pluginConfig.getString(Keys.AMQP_USERNAME.property));
  }

  public String getAMQPPassword() {
    return StringUtils.stripToEmpty(pluginConfig.getString(Keys.AMQP_PASSWORD.property));
  }

  public String getAMQPQueue() {
    return StringUtils.stripToEmpty(pluginConfig.getString(Keys.AMQP_QUEUE.property));
  }

  public String getAMQPExchange() {
    return StringUtils.stripToEmpty(pluginConfig.getString(Keys.AMQP_EXCHANGE.property));
  }

  public String getAMQPRoutingKey() {
    return StringUtils.stripToEmpty(pluginConfig.getString(Keys.AMQP_ROUTINGKEY.property));
  }

  public AMQP.BasicProperties getBasicProperties() {
    if (properties == null) {
      Map<String, Object> headers = new HashMap<String, Object>();
      headers.put(Keys.GERRIT_NAME.header,
          StringUtils.stripToEmpty(pluginConfig.getString(Keys.GERRIT_NAME.property)));
      headers.put(Keys.GERRIT_HOSTNAME.header,
          StringUtils.stripToEmpty(pluginConfig.getString(Keys.GERRIT_HOSTNAME.property)));
      headers.put(Keys.GERRIT_SCHEME.header,
          StringUtils.stripToEmpty(pluginConfig.getString(Keys.GERRIT_SCHEME.property)));
      headers.put(Keys.GERRIT_PORT.header,
          StringUtils.stripToEmpty(pluginConfig.getString(Keys.GERRIT_PORT.property)));
      headers.put(Keys.GERRIT_PORT.header,
          StringUtils.stripToEmpty(pluginConfig.getString(Keys.GERRIT_PORT.property)));
      headers.put(Keys.GERRIT_FRONT_URL.header,
          StringUtils.stripToEmpty(config.getString(GERRIT, null, Keys.GERRIT_FRONT_URL.property)));
      headers.put(Keys.GERRIT_VERSION.header,
          StringUtils.stripToEmpty(Version.getVersion()));

      AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
      builder.appId(GERRIT);
      builder.contentEncoding(CharEncoding.UTF_8);
      builder.contentType(CONTENT_TYPE_JSON);
      builder.headers(headers);

      properties = builder.build();
    }
    return properties;
  }
}
