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

import com.googlesource.gerrit.plugins.rabbitmq.config.Properties;
import com.googlesource.gerrit.plugins.rabbitmq.config.section.AMQP;
import com.googlesource.gerrit.plugins.rabbitmq.config.section.Exchange;
import com.googlesource.gerrit.plugins.rabbitmq.config.section.Message;
import com.googlesource.gerrit.plugins.rabbitmq.config.section.Monitor;
import com.googlesource.gerrit.plugins.rabbitmq.session.Session;
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
        LOGGER.info(MSG("Channel #{} opened."), ch.getChannelNumber());
      } catch (Exception ex) {
        LOGGER.warn(MSG("Failed to open channel."));
        failureCount++;
      }
      if (failureCount > properties.getSection(Monitor.class).failureCount) {
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
    AMQP amqp = properties.getSection(AMQP.class);
    LOGGER.info(MSG("Connect to {}..."), amqp.uri);
    ConnectionFactory factory = new ConnectionFactory();
    try {
      if (StringUtils.isNotEmpty(amqp.uri)) {
        factory.setUri(amqp.uri);
        if (StringUtils.isNotEmpty(amqp.username)) {
          factory.setUsername(amqp.username);
        }
        if (StringUtils.isNotEmpty(amqp.password)) {
          factory.setPassword(amqp.password);
        }
        connection = factory.newConnection();
        connection.addShutdownListener(this);
        LOGGER.info(MSG("Connection established."));
      }
    } catch (URISyntaxException ex) {
      LOGGER.error(MSG("URI syntax error: {}"), amqp.uri);
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
      if (channel != null) {
        LOGGER.info(MSG("Close Channel #{}..."), channel.getChannelNumber());
        channel.close();
      }
    } catch (Exception ex) {
      LOGGER.warn(MSG("Error when close channel.") , ex);
    } finally {
      channel = null;
    }

    try {
      if (connection != null) {
        LOGGER.info(MSG("Close Connection..."));
        connection.close();
      }
    } catch (Exception ex) {
      LOGGER.warn(MSG("Error when close connection.") , ex);
    } finally {
      connection = null;
    }
  }

  @Override
  public void publish(String messageBody) {
    if (channel == null || !channel.isOpen()) {
      channel = getChannel();
    }
    if (channel != null && channel.isOpen()) {
      Message message = properties.getSection(Message.class);
      Exchange exchange = properties.getSection(Exchange.class);
      try {
        LOGGER.debug(MSG("Send message."));
        channel.basicPublish(exchange.name, message.routingKey,
            properties.getAMQProperties().getBasicProperties(),
            messageBody.getBytes(CharEncoding.UTF_8));
      } catch (Exception ex) {
        LOGGER.warn(MSG("Error when sending meessage."), ex);
      }
    }
  }

  @Override
  public void shutdownCompleted(ShutdownSignalException exception) {
    if (exception.isHardError()) {
      if (exception.isInitiatedByApplication()) {
        LOGGER.info(MSG("Connection closed."));
      } else {
        LOGGER.info(MSG("Connection suddenly closed."));
        connection = null;
      }
    } else {
      Channel ch = (Channel) exception.getReference();
      if (exception.isInitiatedByApplication()) {
        LOGGER.info(MSG("Channel #{} closed."), ch.getChannelNumber());
      } else {
        LOGGER.info(MSG("Channel #{} suddenly closed."), ch.getChannelNumber());
        channel = null;
      }
    }
  }
}
