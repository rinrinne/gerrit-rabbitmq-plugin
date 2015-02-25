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
import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

class Module extends AbstractModule {

  private final Properties properties;

  @Inject
  public Module(Properties properties) {
    this.properties = properties;
  }

  @Override
  protected void configure() {
    bind(AMQPSession.class);
//    bind(Properties.class);
    bind(RabbitMQManager.class);
    if (!properties.hasListenAs()) {
      // No listenAs to filter events against. Register an unrestricted ChangeListener
      DynamicSet.bind(binder(), EventListener.class).to(RabbitMQManager.class);
    }
    DynamicSet.bind(binder(), LifecycleListener.class).to(RabbitMQManager.class);
  }
}
