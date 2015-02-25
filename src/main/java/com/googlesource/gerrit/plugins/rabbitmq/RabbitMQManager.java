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

import com.google.gerrit.common.EventListener;
import com.google.gerrit.common.EventSource;
import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.gerrit.reviewdb.client.Account;
import com.google.gerrit.reviewdb.server.ReviewDb;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.PluginUser;
import com.google.gerrit.server.account.AccountResolver;
import com.google.gerrit.server.events.Event;
import com.google.gerrit.server.git.WorkQueue;
import com.google.gerrit.server.util.RequestContext;
import com.google.gerrit.server.util.ThreadLocalRequestContext;
import com.google.gson.Gson;
import com.google.gwtorm.server.OrmException;
import com.google.gwtorm.server.SchemaFactory;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

@Singleton
public class RabbitMQManager implements EventListener, LifecycleListener {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(RabbitMQManager.class);
  private final static int MONITOR_FIRSTTIME_DELAY = 15000;
  private final Properties properties;
  private final AMQPSession session;
  private final Gson gson = new Gson();
  private final Timer monitorTimer = new Timer();
  private final EventSource source;
  private final AccountResolver accountResolver;
  private final IdentifiedUser.GenericFactory userFactory;
  private final WorkQueue workQueue;
  private final ThreadLocalRequestContext threadLocalRequestContext;
  private final PluginUser pluginUser;
  private final SchemaFactory<ReviewDb> schemaFactory;
  private ReviewDb db;
  private Account userAccount;

  @Inject
  public RabbitMQManager(Properties properties,
      AMQPSession session,
      EventSource source,
      AccountResolver accountResolver,
      IdentifiedUser.GenericFactory userFactory,
      WorkQueue workQueue,
      ThreadLocalRequestContext threadLocalRequestContext,
      PluginUser pluginUser,
      SchemaFactory<ReviewDb> schemaFactory) {
    this.properties = properties;
    this.session = session;
    this.source = source;
    this.accountResolver = accountResolver;
    this.userFactory = userFactory;
    this.workQueue = workQueue;
    this.threadLocalRequestContext = threadLocalRequestContext;
    this.pluginUser = pluginUser;
    this.schemaFactory = schemaFactory;
  }

  @Override
  public void start() {
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

    if (properties.hasListenAs()) {
      final String userName = properties.getListenAs();
      final EventListener eventListener = this;
      workQueue.getDefaultQueue().submit(new Runnable() {
        @Override
        public void run() {
          RequestContext old = threadLocalRequestContext
              .setContext(new RequestContext() {

                @Override
                public CurrentUser getCurrentUser() {
                  return pluginUser;
                }

                @Override
                public Provider<ReviewDb> getReviewDbProvider() {
                  return new Provider<ReviewDb>() {
                    @Override
                    public ReviewDb get() {
                      if (db == null) {
                        try {
                          db = schemaFactory.open();
                        } catch (OrmException e) {
                          throw new ProvisionException("Cannot open ReviewDb", e);
                        }
                      }
                      return db;
                    }
                  };
                }
              });
          try {
            userAccount = accountResolver.find(userName);
            if (userAccount == null) {
              LOGGER.error("No single user could be found when searching for listenAs: {}", userName);
              return;
            }

            IdentifiedUser user = userFactory.create(userAccount.getId());
            source.addEventListener(eventListener, user);
            LOGGER.info("Listen events as : {}", userName);
          } catch (OrmException e) {
            LOGGER.error("Could not query database for listenAs", e);
            return;
          } finally {
            threadLocalRequestContext.setContext(old);
            if (db != null) {
              db.close();
              db = null;
            }
          }
        }
      });
    }
  }

  @Override
  public void stop() {
    monitorTimer.cancel();
    session.disconnect();
    source.removeEventListener(this);
  }

  @Override
  public void onEvent(Event event) {
    session.publishMessage(gson.toJson(event));
  }

}
