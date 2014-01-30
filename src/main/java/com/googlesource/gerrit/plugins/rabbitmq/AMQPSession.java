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
    LOGGER.info("Connect to {}...", properties.getString(Keys.AMQP_URI));
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
      setUp();
    } catch (URISyntaxException ex) {
      LOGGER.error("URI syntax error: {}", properties.getString(Keys.AMQP_URI));
    } catch (IOException ex) {
      LOGGER.error("Connection cannot be opened.");
    } catch (Exception ex) {
      LOGGER.warn("Connection has something error. it will be disposed.", ex);
    }
  }

  private void setUp() {
    if (connection != null && publishChannel == null) {
      try {
        Channel ch;
        ch = connection.createChannel();
        LOGGER.info("Open Channel.");

        if (properties.getBoolean(Keys.QUEUE_DECLARE)) {
          LOGGER.info("Declare queue...");
          if (StringUtils.isNotEmpty(properties.getString(Keys.QUEUE_NAME))) {
            try {
              ch.queueDeclarePassive(properties.getString(Keys.QUEUE_NAME));
              LOGGER.info("Queue \"{}\" already exist.", properties.getString(Keys.QUEUE_NAME));
            } catch (Exception ex) {
              createQueue(ch);
            }
          }
        }

        if (properties.getBoolean(Keys.EXCHANGE_DECLARE)) {
          LOGGER.info("Declare exchange...");
          if (StringUtils.isNotEmpty(properties.getString(Keys.EXCHANGE_NAME))) {
            try {
              ch.exchangeDeclarePassive(properties.getString(Keys.EXCHANGE_NAME));
              LOGGER.info("Exchange \"{}\" already exist.", properties.getString(Keys.EXCHANGE_NAME));
            } catch (Exception ex) {
              createExchange(ch);
            }
          }
        }

        if (properties.getBoolean(Keys.BIND_STARTUP)) {
          if (StringUtils.isNotEmpty(properties.getString(Keys.QUEUE_NAME)) &&
              StringUtils.isNotEmpty(properties.getString(Keys.EXCHANGE_NAME))) {
            LOGGER.info("Bind exchange and queue with key: {}", properties.getString(Keys.BIND_ROUTINGKEY));
            ch.queueBind(properties.getString(Keys.QUEUE_NAME),
                properties.getString(Keys.EXCHANGE_NAME),
                properties.getString(Keys.BIND_ROUTINGKEY));
          }
        }

        publishChannel = ch;
        LOGGER.info("Complete to setup channel.");
      } catch (Exception ex) {
        LOGGER.warn("Channel has something error. Connection will be disconnected.", ex);
        disconnect();
      }
    }
  }

  public void createQueue(Channel ch) throws IOException {
    LOGGER.info("Declare queue: {}", properties.getString(Keys.QUEUE_NAME));
    ch.queueDeclare(properties.getString(Keys.QUEUE_NAME),
        properties.getBoolean(Keys.QUEUE_DURABLE),
        properties.getBoolean(Keys.QUEUE_EXCLUSIVE),
        properties.getBoolean(Keys.QUEUE_AUTODELETE), null);
  }

  public void createExchange(Channel ch) throws IOException {
    LOGGER.info("Declare exchange: {}", properties.getString(Keys.EXCHANGE_NAME));
    ch.exchangeDeclare(properties.getString(Keys.EXCHANGE_NAME),
        properties.getString(Keys.EXCHANGE_TYPE),
        properties.getBoolean(Keys.EXCHANGE_DURABLE),
        properties.getBoolean(Keys.EXCHANGE_AUTODELETE), null);
  }

  public void bind(Channel ch) throws IOException {
    LOGGER.info("Bind exchange \"{}\" and queue \"{}\"with key: {}", new Object[]{
        properties.getString(Keys.QUEUE_NAME),
        properties.getString(Keys.EXCHANGE_NAME),
        properties.getString(Keys.BIND_ROUTINGKEY)});
    ch.queueBind(properties.getString(Keys.QUEUE_NAME),
        properties.getString(Keys.EXCHANGE_NAME),
        properties.getString(Keys.BIND_ROUTINGKEY));
  }

  public void disconnect() {
    LOGGER.info("Disconnecting...");
    try {
      if (connection != null) {
        connection.close();
      }
    } catch (Exception ex) {
      LOGGER.warn("Error when close connection." , ex);
    } finally {
      connection = null;
      publishChannel = null;
    }
  }

  public void publishMessage(String message) {
    if (publishChannel != null && publishChannel.isOpen()) {
      try {
        LOGGER.debug("Send message.");
        publishChannel.basicPublish(properties.getString(Keys.EXCHANGE_NAME),
            properties.getString(Keys.MESSAGE_ROUTINGKEY),
            properties.getBasicProperties(),
            message.getBytes(CharEncoding.UTF_8));
      } catch (Exception ex) {
        LOGGER.warn("Error when sending meessage.", ex);
      }
    }
  }

  @Override
  public void shutdownCompleted(ShutdownSignalException exception) {
    Object obj = exception.getReference();

    if (obj instanceof Channel) {
      LOGGER.info("Channel closed.");
      publishChannel = null;
      if (!exception.isInitiatedByApplication()) {
        if (connection != null && connection.isOpen()) {
          try {
            LOGGER.info("Reopen channel...");
            publishChannel = connection.createChannel();
          } catch (Exception ex) {
            LOGGER.warn("Cannot reopen channel. Connection will be disconnected.", ex);
            disconnect();
          }
        }
      }
    } else if (obj instanceof Connection) {
      LOGGER.info("Connection disconnected.");
      if (!exception.isInitiatedByApplication()) {
        connection = null;
      }
    }
  }
}
