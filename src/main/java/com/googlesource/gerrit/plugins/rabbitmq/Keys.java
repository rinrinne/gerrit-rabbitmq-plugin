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

public enum Keys {
  AMQP_URI("amqp.uri", null, "amqp://localhost"),
  AMQP_USERNAME("amqp.username", null, "guest"),
  AMQP_PASSWORD("amqp.password", null, "guest"),
  EXCHANGE_NAME("exchange.name", null, "gerrit.publish"),
  MESSAGE_DELIVERY_MODE("message.deliveryMode", null, 1),
  MESSAGE_PRIORITY("message.priority", null, 0),
  MESSAGE_ROUTINGKEY("message.routingKey", null, ""),
  GERRIT_NAME("gerrit.name", "gerrit-name", ""),
  GERRIT_HOSTNAME("gerrit.hostname", "gerrit-host", ""),
  GERRIT_SCHEME("gerrit.scheme", "gerrit-scheme", "ssh"),
  GERRIT_PORT("gerrit.port", "gerrit-port", 29418),
  GERRIT_FRONT_URL("gerrit.canonicalWebUrl", "gerrit-front-url", ""),
  GERRIT_VERSION("gerrit.version", "gerrit-version", null),
  GERRIT_LISTENAS("gerrit.listenAs", null, null),
  MONITOR_INTERVAL("monitor.interval", null, 15000),
  MONITOR_FAILURECOUNT("monitor.failureCount", null, 15);

  public String section;
  public String name;
  public String key;
  public Object defaultVal;

  Keys(String property, String key, Object defaultVal) {
    String[] part = property.split("\\.");
    this.section = part[0];
    this.name = part[1];
    this.key = key;
    this.defaultVal = defaultVal;
  }
}
