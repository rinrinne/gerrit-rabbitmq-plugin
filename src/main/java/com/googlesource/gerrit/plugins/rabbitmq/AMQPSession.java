package com.googlesource.gerrit.plugins.rabbitmq;

import com.google.inject.Inject;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

public class AMQPSession implements ShutdownListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(AMQPSession.class);
  private final Properties properties;
  private Connection connection;
  private Channel publishChannel;
  private String exchangeName;

  @Inject
  public AMQPSession(Properties properties) {
    this.properties = properties;
  }

  public boolean isOpen() {
    if (connection != null) {
      return true;
    }
    return false;
  }

  public void connect() {
    LOGGER.info("Connect to " + properties.getString(Keys.AMQP_URI) + "...");
    ConnectionFactory factory = new ConnectionFactory();
    try {
      if (StringUtils.isNotEmpty(properties.getString(Keys.AMQP_URI))) {
        factory.setUri(properties.getString(Keys.AMQP_URI));
        if (StringUtils.isNotEmpty(properties.getString(Keys.AMQP_USERNAME))) {
          factory.setUsername(properties.getString(Keys.AMQP_USERNAME));
        }
        if (StringUtils.isNotEmpty(properties.getString(Keys.AMQP_PASSWORD))) {
          factory.setPassword(properties.getString(Keys.AMQP_PASSWORD));
        }
        connection = factory.newConnection();
        connection.addShutdownListener(this);
        LOGGER.info("Connection established.");
      }
      bind();
    } catch (URISyntaxException ex) {
      LOGGER.error("URI syntax error: " + properties.getString(Keys.AMQP_URI));
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
        LOGGER.info("Channel is opened.");
        if (StringUtils.isNotEmpty(properties.getString(Keys.QUEUE_NAME))) {
          LOGGER.info("Queue: " + properties.getString(Keys.QUEUE_NAME));
          ch.queueDeclare(properties.getString(Keys.QUEUE_NAME),
              properties.getBoolean(Keys.QUEUE_DURABLE),
              properties.getBoolean(Keys.QUEUE_EXCLUSIVE),
              properties.getBoolean(Keys.QUEUE_AUTODELETE), null);
          exchangeName = "exchange-for-" + properties.getString(Keys.QUEUE_NAME);
        }

        if (StringUtils.isNotEmpty(properties.getString(Keys.EXCHANGE_NAME))) {
          exchangeName = properties.getString(Keys.EXCHANGE_NAME);
        }

        LOGGER.info("Exchange: " + exchangeName);
        ch.exchangeDeclare(exchangeName,
            properties.getString(Keys.EXCHANGE_TYPE),
            properties.getBoolean(Keys.EXCHANGE_DURABLE),
            properties.getBoolean(Keys.EXCHANGE_AUTODELETE), null);

        if (StringUtils.isNotEmpty(properties.getString(Keys.QUEUE_NAME))) {
          LOGGER.info("Bind exchange and queue with key: " + properties.getString(Keys.BIND_ROUTINGKEY));
          ch.queueBind(properties.getString(Keys.QUEUE_NAME),
              exchangeName, properties.getString(Keys.BIND_ROUTINGKEY));
        }

        publishChannel = ch;
        LOGGER.info("Complete to setup channel.");
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
      connection = null;
      publishChannel = null;
    }
  }

  public void sendMessage(String message) {
    if (publishChannel != null && publishChannel.isOpen()) {
      try {
        LOGGER.debug("Send message.");
        publishChannel.basicPublish(exchangeName, properties.getString(Keys.MESSAGE_ROUTINGKEY), properties.getBasicProperties(),
            message.getBytes(CharEncoding.UTF_8));
      } catch (Exception ex) {
        LOGGER.warn("#sendMessage: " + ex.getClass().getName());
      }
    }
  }

  @Override
  public void shutdownCompleted(ShutdownSignalException arg0) {
    LOGGER.info("Disconnected.");
    connection = null;
    publishChannel = null;
  }
}
