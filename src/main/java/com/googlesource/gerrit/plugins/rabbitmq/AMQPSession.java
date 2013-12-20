package com.googlesource.gerrit.plugins.rabbitmq;

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
  private final Properties properties;
  private Connection connection;
  private Channel publishChannel;
  private String exchangeName;

  interface Factory {
    public AMQPSession create();
  }

  @Inject
  public AMQPSession(Properties properties) {
    this.properties = properties;
  }

  public void connect() {
    ConnectionFactory factory = new ConnectionFactory();
    try {
      if (StringUtils.isNotEmpty(properties.getAMQPUri())) {
        factory.setUri(properties.getAMQPUri());
        if (!StringUtils.isEmpty(properties.getAMQPUsername())) {
          factory.setUsername(properties.getAMQPUsername());
        }
        if (!StringUtils.isEmpty(properties.getAMQPPassword())) {
          factory.setPassword(properties.getAMQPPassword());
        }
        connection = factory.newConnection();
        Channel ch = connection.createChannel();
        if (StringUtils.isNotEmpty(properties.getAMQPQueue())) {
          exchangeName = UUID.randomUUID().toString();
          ch.exchangeDeclare(exchangeName, EXCHANGE_TYPE_DIRECT, true);
          ch.queueDeclare(properties.getAMQPQueue(), true, false, false, null);
          ch.queueBind(properties.getAMQPQueue(), exchangeName, "com.sonymobile.plugin.demo");
          publishChannel = ch;
        } else {
          exchangeName = properties.getAMQPExchange();
          if (StringUtils.isNotEmpty(exchangeName)) {
            ch.exchangeDeclarePassive(exchangeName);
            publishChannel = ch;
          }
        }
      }
    } catch (Exception ex) {
      LOGGER.warn("#connect: " + ex.toString());
    }
  }

  public void disconnect() {
    if (connection != null) {
      try {
        publishChannel.close();
        connection.close();
      } catch (Exception ex) {
        LOGGER.warn("#disconnect: " + ex.toString());
        ex.printStackTrace();
      } finally {
        connection = null;
        publishChannel = null;
      }
    }
  }

  public void sendMessage(String message) {
    if (publishChannel != null && publishChannel.isOpen()) {
      try {
        publishChannel.basicPublish(exchangeName, properties.getAMQPRoutingKey(), null, message.getBytes(CharEncoding.UTF_8));
      } catch (Exception ex) {
        LOGGER.warn("#sendMessage: " + ex.toString());
      }
    }
  }
}
