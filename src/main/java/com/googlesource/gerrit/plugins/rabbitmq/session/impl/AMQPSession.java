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

package com.googlesource.gerrit.plugins.rabbitmq.session.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import com.googlesource.gerrit.plugins.rabbitmq.Keys;
import com.googlesource.gerrit.plugins.rabbitmq.config.Properties;
import com.googlesource.gerrit.plugins.rabbitmq.session.Session;
// import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
// import com.rabbitmq.client.DefaultConsumer;
// import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

public final class AMQPSession implements Session, ShutdownListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(AMQPSession.class);
  private final Properties properties;
  private volatile Connection connection;
  private volatile Channel channel;
  private volatile int failureCount = 0;

  @Inject
  public AMQPSession(@Assisted Properties properties) {
    this.properties = properties;
  }

  private String MSG(String msg) {
    return String.format("[%s] %s", properties.getName(), msg);
  }

  @Override
  public Properties getProperties() {
    return properties;
  }

  @Override
  public boolean isOpen() {
    if (connection != null) {
      return true;
    }
    return false;
  }

  private Channel getChannel() {
    Channel ch = null;
    if (connection == null) {
      connect();
    } else {
      try {
        ch = connection.createChannel();
        ch.addShutdownListener(this);
        failureCount = 0;
        LOGGER.info(MSG("Publish channel opened."));
      } catch (Exception ex) {
        LOGGER.warn(MSG("Failed to open publish channel."));
        failureCount++;
      }
      if (failureCount > properties.getConnectionMonitorInterval()) {
        LOGGER.warn("Connection has something wrong. So will be disconnected.");
        disconnect();
      }
    }
    return ch;
  }

  @Override
  public void connect() {
    if (connection != null && connection.isOpen()) {
      LOGGER.info(MSG("Already connected."));
      return;
    }
    LOGGER.info(MSG("Connect to {}..."), properties.getString(Keys.AMQP_URI));
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
        LOGGER.info(MSG("Connection established."));
      }
    } catch (URISyntaxException ex) {
      LOGGER.error(MSG("URI syntax error: {}"), properties.getString(Keys.AMQP_URI));
    } catch (IOException ex) {
      LOGGER.error(MSG("Connection cannot be opened."));
    } catch (Exception ex) {
      LOGGER.warn(MSG("Connection has something error. it will be disposed."), ex);
    }
  }

  @Override
  public void disconnect() {
    LOGGER.info(MSG("Disconnecting..."));
    try {
      if (connection != null) {
        connection.close();
      }
    } catch (Exception ex) {
      LOGGER.warn(MSG("Error when close connection.") , ex);
    } finally {
      connection = null;
      channel = null;
    }
  }

  @Override
  public void publish(String message) {
    if (channel == null || !channel.isOpen()) {
      channel = getChannel();
    }
    if (channel != null && channel.isOpen()) {
      try {
        LOGGER.debug(MSG("Send message."));
        channel.basicPublish(properties.getString(Keys.EXCHANGE_NAME),
            properties.getString(Keys.MESSAGE_ROUTINGKEY),
            properties.getAMQProperties().getBasicProperties(),
            message.getBytes(CharEncoding.UTF_8));
      } catch (Exception ex) {
        LOGGER.warn(MSG("Error when sending meessage."), ex);
      }
    }
  }

  @Override
  public void shutdownCompleted(ShutdownSignalException exception) {
    Object obj = exception.getReference();

    if (obj instanceof Channel) {
      Channel ch = (Channel) obj;
      if (ch.equals(channel)) {
        LOGGER.info(MSG("Publish channel closed."));
        channel = null;
      }
    } else if (obj instanceof Connection) {
      LOGGER.info(MSG("Connection disconnected."));
      connection = null;
    }
  }
}
