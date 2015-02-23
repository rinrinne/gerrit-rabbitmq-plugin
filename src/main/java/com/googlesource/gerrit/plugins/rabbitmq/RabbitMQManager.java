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

import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class RabbitMQManager implements LifecycleListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQManager.class);
  private final DefaultMessagePublisher defaultMessagePublisher;
  private final MessagePublisher.Factory publisherFactory;
  private final AMQPSession.Factory sessionFactory;
  private final PropertiesStore propertiesStore;
  private final BCSolver bcSolver;
  private final List<MessagePublisher> publisherList = new ArrayList<>();

  @Inject
  public RabbitMQManager(
      DefaultMessagePublisher defaultMessagePublisher,
      MessagePublisher.Factory publisherFactory,
      AMQPSession.Factory sessionFactory,
      PropertiesStore propertiesStore,
      BCSolver bcSolver) {
    this.defaultMessagePublisher = defaultMessagePublisher;
    this.publisherFactory = publisherFactory;
    this.sessionFactory = sessionFactory;
    this.propertiesStore = propertiesStore;
    this.bcSolver = bcSolver;
  }

  @Override
  public void start() {
    bcSolver.solve();
    propertiesStore.load();
    for (Properties properties : propertiesStore) {
      AMQPSession session = sessionFactory.create(properties);
      if (properties.hasListenAs()) {
        MessagePublisher publisher = publisherFactory.create(session);
        publisher.start();
        publisherList.add(publisher);
      } else {
        defaultMessagePublisher.addSession(session);
      }
    }
  }

  @Override
  public void stop() {
    for (MessagePublisher publisher : publisherList) {
      publisher.stop();
    }
    publisherList.clear();
  }
}
