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

class Module extends AbstractModule {

  @Override
  protected void configure() {
    bind(AMQPSession.class);
    bind(Properties.class);
    bind(MessagePublisher.class);
    bind(DefaultMessagePublisher.class);
    bind(RabbitMQManager.class);
    DynamicSet.bind(binder(), LifecycleListener.class).to(RabbitMQManager.class);
    DynamicSet.bind(binder(), ChangeListener.class).to(DefaultMessagePublisher.class);
  }
}
