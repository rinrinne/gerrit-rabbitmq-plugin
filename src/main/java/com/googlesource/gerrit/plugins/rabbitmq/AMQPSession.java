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
        if (properties.getBoolean(Keys.QUEUE_DECLARE)) {
          LOGGER.info("Declare Queue...");
          if (StringUtils.isNotEmpty(properties.getString(Keys.QUEUE_NAME))) {
            LOGGER.info("Declare queue: " + properties.getString(Keys.QUEUE_NAME));
            ch.queueDeclare(properties.getString(Keys.QUEUE_NAME),
                properties.getBoolean(Keys.QUEUE_DURABLE),
                properties.getBoolean(Keys.QUEUE_EXCLUSIVE),
                properties.getBoolean(Keys.QUEUE_AUTODELETE), null);
          }

          if (properties.getBoolean(Keys.EXCHANGE_DECLARE)) {
            LOGGER.info("Declare exchange: " + properties.getString(Keys.EXCHANGE_NAME));
            ch.exchangeDeclare(properties.getString(Keys.EXCHANGE_NAME),
                properties.getString(Keys.EXCHANGE_TYPE),
                properties.getBoolean(Keys.EXCHANGE_DURABLE),
                properties.getBoolean(Keys.EXCHANGE_AUTODELETE), null);
          }

          if (properties.getBoolean(Keys.BIND_STARTUP)) {
            if (StringUtils.isNotEmpty(properties.getString(Keys.QUEUE_NAME))) {
              LOGGER.info("Bind exchange and queue with key: " + properties.getString(Keys.BIND_ROUTINGKEY));
              ch.queueBind(properties.getString(Keys.QUEUE_NAME),
                  properties.getString(Keys.EXCHANGE_NAME),
                  properties.getString(Keys.BIND_ROUTINGKEY));
            }
          }

          publishChannel = ch;
          LOGGER.info("Complete to setup channel.");
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
