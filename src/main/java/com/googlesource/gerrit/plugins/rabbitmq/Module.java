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

import com.google.gerrit.common.ChangeListener;
import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import com.googlesource.gerrit.plugins.rabbitmq.message.DefaultChangeListener;
import com.googlesource.gerrit.plugins.rabbitmq.message.IdentifiedChangeListener;
import com.googlesource.gerrit.plugins.rabbitmq.message.MessagePublisher;
import com.googlesource.gerrit.plugins.rabbitmq.message.Publisher;
import com.googlesource.gerrit.plugins.rabbitmq.message.PublisherFactory;
import com.googlesource.gerrit.plugins.rabbitmq.session.Session;
import com.googlesource.gerrit.plugins.rabbitmq.session.SessionFactory;
import com.googlesource.gerrit.plugins.rabbitmq.session.impl.AMQPSession;
import com.googlesource.gerrit.plugins.rabbitmq.solver.BCSolver;
import com.googlesource.gerrit.plugins.rabbitmq.solver.Solver;
import com.googlesource.gerrit.plugins.rabbitmq.solver.SolverFactory;

class Module extends AbstractModule {

  @Override
  protected void configure() {
    bind(PropertiesStore.class);
    bind(BCSolver.class);
    bind(IdentifiedChangeListener.class);
    bind(RabbitMQManager.class);

    install(new FactoryModuleBuilder().implement(Solver.class, BCSolver.class).build(SolverFactory.class));
    install(new FactoryModuleBuilder().implement(Session.class, AMQPSession.class).build(SessionFactory.class));
    install(new FactoryModuleBuilder().implement(Publisher.class, MessagePublisher.class).build(PublisherFactory.class));

    DynamicSet.bind(binder(), LifecycleListener.class).to(RabbitMQManager.class);
    DynamicSet.bind(binder(), LifecycleListener.class).to(DefaultChangeListener.class);
    DynamicSet.bind(binder(), ChangeListener.class).to(DefaultChangeListener.class);
  }
}
