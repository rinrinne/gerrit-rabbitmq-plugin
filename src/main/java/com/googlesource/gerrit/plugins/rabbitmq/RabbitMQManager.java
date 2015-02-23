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

@Singleton
public class RabbitMQManager implements LifecycleListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQManager.class);
  private final MessagePublisher messagePublisher;
  private final DefaultMessagePublisher defaultMessagePublisher;
  private final Properties properties;

  @Inject
  public RabbitMQManager(
      MessagePublisher messagePublisher,
      DefaultMessagePublisher defaultMessagePublisher,
      Properties properties) {
    this.messagePublisher = messagePublisher;
    this.defaultMessagePublisher = defaultMessagePublisher;
    this.properties = properties;
  }

  @Override
  public void start() {
    if (properties.hasListenAs()) {
      messagePublisher.start();
    } else {
      defaultMessagePublisher.start();
    }
  }

  @Override
  public void stop() {
    if (properties.hasListenAs()) {
      messagePublisher.stop();
    } else {
      defaultMessagePublisher.stop();
    }
  }
}
