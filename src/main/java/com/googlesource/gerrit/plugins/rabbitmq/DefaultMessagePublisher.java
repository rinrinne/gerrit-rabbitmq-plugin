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

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;

@Singleton
public class DefaultMessagePublisher implements ChangeListener, LifecycleListener {

  public static class SessionEntry {
    public AMQPSession session;
    public Timer monitorTimer;
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMessagePublisher.class);

  private final static int MONITOR_FIRSTTIME_DELAY = 15000;

  private final Set<SessionEntry> sessionEntries = new CopyOnWriteArraySet<>();
  private final Gson gson;

  @Inject
  public DefaultMessagePublisher(
      final Gson gson) {
    this.gson = gson;
  }

  private void openSession(final SessionEntry entry) {
    if (!entry.session.isOpen()) {
      entry.session.connect();
      entry.monitorTimer = new Timer();
      entry.monitorTimer.schedule(new TimerTask() {
        @Override
        public void run() {
          if (!entry.session.isOpen()) {
            LOGGER.info("#start: try to reconnect");
            entry.session.connect();
          }
        }
      }, MONITOR_FIRSTTIME_DELAY, entry.session.getProperties().getInt(Keys.MONITOR_INTERVAL));
    }
  }
  public void addSession(final AMQPSession session) {
    SessionEntry entry = new SessionEntry();
    entry.session = session;
    openSession(entry);
    sessionEntries.add(entry);
  }

  @Override
  public void start() {
    LOGGER.info("Start default listener.");
    for (SessionEntry entry : sessionEntries) {
      openSession(entry);
    }
  }

  @Override
  public void stop() {
    LOGGER.info("Stop default listener.");
    for (SessionEntry entry : sessionEntries) {
      entry.monitorTimer.cancel();
      entry.session.disconnect();
    }
  }

  @Override
  public void onChangeEvent(ChangeEvent event) {
    for (SessionEntry entry : sessionEntries) {
      if (entry.session.isOpen()) {
        entry.session.publishMessage(gson.toJson(event));
      }
    }
  }
}
