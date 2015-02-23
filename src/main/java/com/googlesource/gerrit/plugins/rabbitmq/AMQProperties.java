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

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import com.rabbitmq.client.AMQP;

import org.apache.commons.codec.CharEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class AMQProperties {

  interface Factory {
    AMQProperties create(Properties properties);
  }

  public final static String EVENT_APPID = "gerrit";
  public final static String CONTENT_TYPE_JSON = "application/json";

  private static final Logger LOGGER = LoggerFactory.getLogger(AMQProperties.class);

  private final Properties properties;
  private AMQP.BasicProperties amqpProperties;

  @Inject
  public AMQProperties(@Assisted Properties properties) {
    this.properties = properties;
  }

  public AMQP.BasicProperties getBasicProperties() {
    if (amqpProperties == null) {
      Map<String, Object> headers = new HashMap<>();
      headers.put(Keys.GERRIT_NAME.key, properties.getString(Keys.GERRIT_NAME));
      headers.put(Keys.GERRIT_HOSTNAME.key, properties.getString(Keys.GERRIT_HOSTNAME));
      headers.put(Keys.GERRIT_SCHEME.key, properties.getString(Keys.GERRIT_SCHEME));
      headers.put(Keys.GERRIT_PORT.key, String.valueOf(properties.getInt(Keys.GERRIT_PORT)));
      headers.put(Keys.GERRIT_FRONT_URL.key, properties.getGerritFrontUrl());
      headers.put(Keys.GERRIT_VERSION.key, properties.getGerritVersion());

      AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
      builder.appId(EVENT_APPID);
      builder.contentEncoding(CharEncoding.UTF_8);
      builder.contentType(CONTENT_TYPE_JSON);
      builder.deliveryMode(properties.getInt(Keys.MESSAGE_DELIVERY_MODE));
      builder.priority(properties.getInt(Keys.MESSAGE_PRIORITY));
      builder.headers(headers);

      amqpProperties = builder.build();
    }
    return amqpProperties;
  }
}
