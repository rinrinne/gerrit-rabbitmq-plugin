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

import com.googlesource.gerrit.plugins.rabbitmq.config.Properties;
import com.googlesource.gerrit.plugins.rabbitmq.message.DefaultChangeListener;
import com.googlesource.gerrit.plugins.rabbitmq.message.IdentifiedChangeListener;
import com.googlesource.gerrit.plugins.rabbitmq.message.MessagePublisher;
import com.googlesource.gerrit.plugins.rabbitmq.message.Publisher;
import com.googlesource.gerrit.plugins.rabbitmq.message.PublisherFactory;
import com.googlesource.gerrit.plugins.rabbitmq.session.Session;
import com.googlesource.gerrit.plugins.rabbitmq.session.SessionFactory;
import com.googlesource.gerrit.plugins.rabbitmq.solver.BCSolver;
import com.googlesource.gerrit.plugins.rabbitmq.solver.Solver;
import com.googlesource.gerrit.plugins.rabbitmq.solver.SolverFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class RabbitMQManager implements LifecycleListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQManager.class);
  private final DefaultChangeListener defaultChangeListener;
  private final IdentifiedChangeListener identifiedChangeListener;
  private final PublisherFactory publisherFactory;
  private final PropertiesStore propertiesStore;
  private final SolverFactory solverFactory;
  private final List<Publisher> publisherList = new ArrayList<>();

  @Inject
  public RabbitMQManager(
      DefaultChangeListener defaultChangeListener,
      IdentifiedChangeListener identifiedChangeListener,
      PublisherFactory publisherFactory,
      PropertiesStore propertiesStore,
      SolverFactory solverFactory) {
    this.defaultChangeListener = defaultChangeListener;
    this.identifiedChangeListener = identifiedChangeListener;
    this.publisherFactory = publisherFactory;
    this.propertiesStore = propertiesStore;
    this.solverFactory = solverFactory;
  }

  @Override
  public void start() {
    Solver solver = solverFactory.create();
    solver.solve();

    propertiesStore.load();
    for (Properties properties : propertiesStore) {
      Publisher publisher = publisherFactory.create(properties);
      publisher.start();
      if (properties.hasListenAs()) {
        identifiedChangeListener.addPublisher(publisher, properties.getListenAs());
      } else {
        defaultChangeListener.addPublisher(publisher);
      }
      publisherList.add(publisher);
    }
  }

  @Override
  public void stop() {
    for (Publisher publisher : publisherList) {
      publisher.stop();
      if (publisher.getSession().getProperties().hasListenAs()) {
        identifiedChangeListener.removePublisher(publisher);
      } else {
        defaultChangeListener.removePublisher(publisher);
      }
    }
    publisherList.clear();
  }
}
