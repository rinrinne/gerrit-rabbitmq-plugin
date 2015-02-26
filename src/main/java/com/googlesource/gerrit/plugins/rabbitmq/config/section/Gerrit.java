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

package com.googlesource.gerrit.plugins.rabbitmq.config.section;

import com.google.gerrit.common.Version;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.rabbitmq.annotation.Default;
import com.googlesource.gerrit.plugins.rabbitmq.annotation.MessageHeader;

import org.eclipse.jgit.lib.Config;

public class Gerrit implements Section {

  @Default
  @MessageHeader("gerrit-name")
  public String name;

  @Default
  @MessageHeader("gerrit-host")
  public String hostname;

  @Default("ssh")
  @MessageHeader("gerrit-scheme")
  public String scheme;

  @Default("29418")
  @MessageHeader("gerrit-port")
  public Integer port;

  @MessageHeader("gerrit-front-url")
  public String canonicalWebUrl;

  @MessageHeader("gerrit-version")
  public String version;

  @Default
  public String listenAs;

  @Inject
  public Gerrit(final @GerritServerConfig Config config) {
    this.canonicalWebUrl = config.getString("gerrit", null, "canonicalWebUrl");
    this.version = Version.getVersion();
  }

}
