package com.googlesource.gerrit.plugins.rabbitmq;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.inject.Inject;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

public class AMQPSession {

  private static final Logger LOGGER = LoggerFactory.getLogger(AMQPSession.class);
  private static final String EXCHANGE_TYPE_DIRECT ="direct";
  private static final String EXCHANGE_TYPE_FANOUT ="fanout";
  private final Properties properties;
  private final String pluginName;
  private Connection connection;
  private Channel publishChannel;
  private String exchangeName;

  @Inject
  public AMQPSession(@PluginName String pluginName, Properties properties) {
    this.properties = properties;
    this.pluginName = pluginName;
  }

  public boolean isOpen() {
    if (connection != null) {
      return true;
    }
    return false;
  }

  public void connect() {
    LOGGER.info("Connect to " + properties.getAMQPUri() + "...");
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
        LOGGER.info("Connection established.");
      }
      bind();
    } catch (URISyntaxException ex) {
      LOGGER.error("URI syntax error: " + properties.getAMQPUri());
    } catch (IOException ex) {
      LOGGER.error("Connection cannot be opened.");
    } catch (Exception ex) {
      LOGGER.warn("#connect: " + ex.getClass().getName());
    }
  }

  private void bind() {
    if (connection != null && publishChannel == null) {
      try {
        Channel ch = connection.createChannel();
        if (StringUtils.isNotEmpty(properties.getAMQPQueue())) {
          LOGGER.info("Queue mode");
          String exchangeType = EXCHANGE_TYPE_DIRECT;
          String routingKey = properties.getAMQPRoutingKey();
          if (routingKey.isEmpty()) {
            exchangeType = EXCHANGE_TYPE_FANOUT;
            routingKey = pluginName;
          }
          LOGGER.debug("Exchange type: " + exchangeType);
          LOGGER.debug("Declare exchange: " + exchangeName);
          ch.exchangeDeclare(exchangeName, exchangeType, true);
          LOGGER.debug("Declare queue: " + properties.getAMQPQueue());
          ch.queueDeclare(properties.getAMQPQueue(), true, false, false, null);
          LOGGER.debug("Bind exchange and queue with key: " + routingKey);
          ch.queueBind(properties.getAMQPQueue(), exchangeName, routingKey);
          publishChannel = ch;
          LOGGER.info("Channel for queue \"" + properties.getAMQPQueue() + "\" opened.");
        } else {
          LOGGER.info("Exchange mode");
          exchangeName = properties.getAMQPExchange();
          if (StringUtils.isNotEmpty(exchangeName)) {
            LOGGER.debug("Declare exchange: " + exchangeName);
            ch.exchangeDeclarePassive(exchangeName);
            publishChannel = ch;
            LOGGER.info("Channel for exchange \"" + exchangeName + "\" opened.");
          }
        }
      } catch (Exception ex) {
        LOGGER.warn("#bind: " + ex.getClass().getName());
        disconnect();
      }
    }
  }

  public void disconnect() {
    LOGGER.info("Disconnecting...");
    try {
      if (connection != null) {
        connection.close();
      }
    } catch (Exception ex) {
      LOGGER.warn("#disconnect: " + ex.getClass().getName());
    } finally {
      connection = null;
      publishChannel = null;
    }
    LOGGER.info("Disconnected.");
  }

  public void sendMessage(String message) {
    if (publishChannel != null && publishChannel.isOpen()) {
      try {
        LOGGER.debug("Send message.");
        publishChannel.basicPublish(exchangeName, properties.getAMQPRoutingKey(), properties.getBasicProperties(),
            message.getBytes(CharEncoding.UTF_8));
      } catch (Exception ex) {
        LOGGER.warn("#sendMessage: " + ex.getClass().getName());
      }
    }
  }
}
