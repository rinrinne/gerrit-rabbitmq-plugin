// Copyright (C) 2015 The Android Open Source Project
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

package com.googlesource.gerrit.plugins.rabbitmq.message;

import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.gerrit.server.events.ChangeEvent;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import com.googlesource.gerrit.plugins.rabbitmq.config.Properties;
import com.googlesource.gerrit.plugins.rabbitmq.config.section.Monitor;
import com.googlesource.gerrit.plugins.rabbitmq.session.Session;
import com.googlesource.gerrit.plugins.rabbitmq.session.SessionFactoryProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class MessagePublisher implements Publisher, LifecycleListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessagePublisher.class);

  private final static int MONITOR_FIRSTTIME_DELAY = 15000;

  private final Session session;
  private final Properties properties;
  private final Gson gson;
  private final Timer monitorTimer = new Timer();
  private boolean available = true;

  @Inject
  public MessagePublisher(
      @Assisted Properties properties,
      SessionFactoryProvider sessionFactoryProvider,
      Gson gson) {
    this.session = sessionFactoryProvider.get().create(properties);
    this.properties = properties;
    this.gson = gson;
  }

  @Override
  public void start() {
    if (!session.isOpen()) {
      session.connect();
      monitorTimer.schedule(new TimerTask() {
        @Override
        public void run() {
          if (!session.isOpen()) {
            LOGGER.info("#start: try to reconnect");
            session.connect();
          }
        }
      }, MONITOR_FIRSTTIME_DELAY, properties.getSection(Monitor.class).interval);
      available = true;
    }
  }

  @Override
  public void stop() {
    monitorTimer.cancel();
    session.disconnect();
    available = false;
  }

  @Override
  public void onChangeEvent(ChangeEvent event) {
    if (available && session.isOpen()) {
      session.publish(gson.toJson(event));
    }
  }

  @Override
  public void enable() {
    available = true;
  }

  @Override
  public void disable() {
    available = false;
  }

  @Override
  public boolean isEnable() {
    return available;
  }

  @Override
  public Session getSession() {
    return session;
  }

  @Override
  public Properties getProperties() {
    return properties;
  }

  @Override
  public String getName() {
    return properties.getName();
  }
}
