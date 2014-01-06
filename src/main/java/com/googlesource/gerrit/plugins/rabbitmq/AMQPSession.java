package com.googlesource.gerrit.plugins.rabbitmq;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Singleton
public class AMQPSession {

  private static final Logger LOGGER = LoggerFactory.getLogger(AMQPSession.class);
  private static final String EXCHANGE_TYPE_DIRECT ="direct";
  private static final String EXCHANGE_TYPE_FANOUT ="fanout";
  private final Properties properties;
  private final String pluginName;
  private Connection connection;
  private Channel publishChannel;
  private String exchangeName;

  interface Factory {
    public AMQPSession create();
  }

  @Inject
  public AMQPSession(@PluginName String pluginName, Properties properties) {
    this.properties = properties;
    this.pluginName = pluginName;
  }

  public void connect() {
    ConnectionFactory factory = new ConnectionFactory();
    exchangeName = UUID.randomUUID().toString();
    try {
      if (StringUtils.isNotEmpty(properties.getAMQPUri())) {
        factory.setUri(properties.getAMQPUri());
        if (StringUtils.isNotEmpty(properties.getAMQPUsername())) {
          factory.setUsername(properties.getAMQPUsername());
        }
        if (StringUtils.isNotEmpty(properties.getAMQPPassword())) {
          factory.setPassword(properties.getAMQPPassword());
        }
        connection = factory.newConnection();
      }
      bind();
    } catch (Exception ex) {
      LOGGER.warn("#connect: " + ex.toString());
    }
  }

  private void bind() {
    if (connection != null && publishChannel == null) {
      try {
        Channel ch = connection.createChannel();
        if (StringUtils.isNotEmpty(properties.getAMQPQueue())) {
          String exchangeType = EXCHANGE_TYPE_DIRECT;;
          String routingKey = properties.getAMQPRoutingKey();
          if (routingKey.isEmpty()) {
            exchangeType = EXCHANGE_TYPE_FANOUT;
            routingKey = pluginName;
          }
          ch.exchangeDeclare(exchangeName, exchangeType, true);
          ch.queueDeclare(properties.getAMQPQueue(), true, false, false, null);
          ch.queueBind(properties.getAMQPQueue(), exchangeName, routingKey);
          publishChannel = ch;
        } else {
          exchangeName = properties.getAMQPExchange();
          if (StringUtils.isNotEmpty(exchangeName)) {
            ch.exchangeDeclarePassive(exchangeName);
            publishChannel = ch;
          }
        }
      } catch (Exception ex) {
        LOGGER.warn("#bind: " + ex.toString());
        disconnect();
      }
    }
  }

  public void disconnect() {
    try {
      if (connection != null) {
        connection.close();
      }
    } catch (Exception ex) {
      LOGGER.warn("#disconnect: " + ex.toString());
      ex.printStackTrace();
    } finally {
      connection = null;
      publishChannel = null;
    }
  }

  public void sendMessage(String message) {
    if (publishChannel != null && publishChannel.isOpen()) {
      try {
        publishChannel.basicPublish(exchangeName, properties.getAMQPRoutingKey(), properties.getBasicProperties(),
            message.getBytes(CharEncoding.UTF_8));
      } catch (Exception ex) {
        LOGGER.warn("#sendMessage: " + ex.toString());
      }
    }
  }
}
