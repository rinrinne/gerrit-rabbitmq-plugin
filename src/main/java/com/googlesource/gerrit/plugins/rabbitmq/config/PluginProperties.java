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

package com.googlesource.gerrit.plugins.rabbitmq.config;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import com.googlesource.gerrit.plugins.rabbitmq.config.section.Gerrit;
import com.googlesource.gerrit.plugins.rabbitmq.config.section.Monitor;
import com.googlesource.gerrit.plugins.rabbitmq.config.section.Section;
import com.googlesource.gerrit.plugins.rabbitmq.config.section.Sections;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

public class PluginProperties implements Properties {

  private static final Logger LOGGER = LoggerFactory.getLogger(PluginProperties.class);

  private final static int MINIMUM_CONNECTION_MONITOR_INTERVAL = 5000;

  private final Set<Section> sections;
  private final Path propertiesFile;
  private AMQProperties amqProperties;

  @AssistedInject
  public PluginProperties(Set<Section> sections) {
    this(sections, null);
  }

  @AssistedInject
  public PluginProperties(Set<Section> sections, @Assisted Path propertiesFile) {
    this.sections = sections;
    this.propertiesFile = propertiesFile;
  }

  private void initialize() {
    for (Section section : sections) {
      Sections.initialize(section);
    }
  }

  @Override
  public Config toConfig() {
    Config config = new Config();
    for (Section section : sections) {
      config = Sections.toConfig(section, config);
    }
    return config;
  }

  @Override
  public boolean load() {
    return load(null);
  }

  @Override
  public boolean load(Properties baseProperties) {
    initialize();
    LOGGER.info("Loading {} ...", propertiesFile);
    if (!Files.exists(propertiesFile)) {
      LOGGER.warn("No {}", propertiesFile);
      return false;
    }

    FileBasedConfig cfg = new FileBasedConfig(propertiesFile.toFile(), FS.DETECTED);
    try {
      cfg.load();
    } catch (ConfigInvalidException e) {
      LOGGER.info("{} has invalid format: {}", propertiesFile, e.getMessage());
      return false;
    } catch (IOException e) {
      LOGGER.info("Cannot read {}: {}", propertiesFile, e.getMessage());
      return false;
    }
    for (Section section : getSections()) {
      if (baseProperties != null) {
        Sections.fromConfig(section, baseProperties.toConfig(), cfg);
      } else {
        Sections.fromConfig(section, cfg);
      }
      Sections.normalize(section);
    }
    return true;
  }

  @Override
  public Path getPath() {
    return propertiesFile;
  }

  @Override
  public String getName() {
    if (propertiesFile != null) {
      return FilenameUtils.removeExtension(propertiesFile.getFileName().toString());
    }
    return null;
  }

  @Override
  public Set<Section> getSections() {
    return Collections.unmodifiableSet(sections);
  }

  @Override
  public <T extends Section> T getSection(Class<T> clazz) {
    for (Section section : sections) {
      if (section.getClass() == clazz) {
        return clazz.cast(section);
      }
    }
    return null;
  }

  public String getGerritFrontUrl() {
    Gerrit gerrit = (Gerrit) getSection(Gerrit.class);
    if (gerrit != null) {
      return gerrit.canonicalWebUrl;
    }
    return null;
  }

  public boolean hasListenAs() {
    Gerrit gerrit = (Gerrit) getSection(Gerrit.class);
    if (gerrit != null) {
      return gerrit.listenAs.isEmpty();
    }
    return false;
  }

  public String getListenAs() {
    Gerrit gerrit = (Gerrit) getSection(Gerrit.class);
    if (gerrit != null) {
      return gerrit.listenAs;
    }
    return null;
  }

  public String getGerritVersion() {
    Gerrit gerrit = (Gerrit) getSection(Gerrit.class);
    if (gerrit != null) {
      return gerrit.version;
    }
    return null;
  }

  public int getConnectionMonitorInterval() {
    Monitor monitor = (Monitor) getSection(Monitor.class);
    if (monitor != null && monitor.interval < MINIMUM_CONNECTION_MONITOR_INTERVAL) {
      return monitor.interval;
    }
    return MINIMUM_CONNECTION_MONITOR_INTERVAL;
  }

  public AMQProperties getAMQProperties() {
    if (amqProperties == null) {
      amqProperties = new AMQProperties(this);
    }
    return amqProperties;
  }
}
