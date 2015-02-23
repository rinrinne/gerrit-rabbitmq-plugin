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

package com.googlesource.gerrit.plugins.rabbitmq;

import com.google.gerrit.common.ChangeListener;
import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.gerrit.server.events.ChangeEvent;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

@Singleton
public class DefaultMessagePublisher implements ChangeListener, LifecycleListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMessagePublisher.class);

  private final static int MONITOR_FIRSTTIME_DELAY = 15000;

  private final Properties properties;
  private final AMQPSession session;
  private final Gson gson;
  private final Timer monitorTimer = new Timer();

  @Inject
  public DefaultMessagePublisher(
      final Properties properties,
      final AMQPSession session,
      final Gson gson) {
    this.properties = properties;
    this.session = session;
    this.gson = gson;
  }

  @Override
  public void start() {
    LOGGER.info("Start default listener.");
    session.connect();
    monitorTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        if (!session.isOpen()) {
          LOGGER.info("#start: try to reconnect");
          session.connect();
        }
      }
    }, MONITOR_FIRSTTIME_DELAY, properties.getInt(Keys.MONITOR_INTERVAL));
  }

  @Override
  public void stop() {
    LOGGER.info("Stop default listener.");
    monitorTimer.cancel();
    session.disconnect();
  }

  @Override
  public void onChangeEvent(ChangeEvent event) {
    if (session.isOpen()) {
      session.publishMessage(gson.toJson(event));
    }
  }
}
