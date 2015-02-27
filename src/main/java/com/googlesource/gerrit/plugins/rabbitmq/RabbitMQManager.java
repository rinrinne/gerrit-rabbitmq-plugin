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

import com.google.gerrit.extensions.annotations.PluginData;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.googlesource.gerrit.plugins.rabbitmq.config.Properties;
import com.googlesource.gerrit.plugins.rabbitmq.config.PropertiesFactory;
import com.googlesource.gerrit.plugins.rabbitmq.config.section.Gerrit;
import com.googlesource.gerrit.plugins.rabbitmq.message.Publisher;
import com.googlesource.gerrit.plugins.rabbitmq.message.PublisherFactory;
import com.googlesource.gerrit.plugins.rabbitmq.solver.Solver;
import com.googlesource.gerrit.plugins.rabbitmq.solver.SolverFactory;
import com.googlesource.gerrit.plugins.rabbitmq.worker.ChangeWorker;
import com.googlesource.gerrit.plugins.rabbitmq.worker.ChangeWorkerFactory;
import com.googlesource.gerrit.plugins.rabbitmq.worker.DefaultChangeWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class RabbitMQManager implements LifecycleListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQManager.class);

  public static final String FILE_EXT = ".config";
  public static final String SITE_DIR = "site";

  private final String pluginName;
  private final Path pluginDataDir;
  private final ChangeWorker defaultChangeWorker;
  private final ChangeWorker userChangeWorker;
  private final PublisherFactory publisherFactory;
  private final PropertiesFactory propFactory;
  private final SolverFactory solverFactory;
  private final List<Publisher> publisherList = new ArrayList<>();

  @Inject
  public RabbitMQManager(
      @PluginName final String pluginName,
      @PluginData final File pluginData,
      final DefaultChangeWorker defaultChangeWorker,
      final ChangeWorkerFactory changeWorkerFactory,
      final PublisherFactory publisherFactory,
      final PropertiesFactory propFactory,
      final SolverFactory solverFactory) {
    this.pluginName = pluginName;
    this.pluginDataDir = pluginData.toPath();
    this.defaultChangeWorker = defaultChangeWorker;
    this.userChangeWorker = changeWorkerFactory.create();
    this.publisherFactory = publisherFactory;
    this.propFactory = propFactory;
    this.solverFactory = solverFactory;
  }

  @Override
  public void start() {
    Solver solver = solverFactory.create();
    solver.solve();

    List<Properties> propList = load();
    for (Properties properties : propList) {
      Publisher publisher = publisherFactory.create(properties);
      publisher.start();
      String listenAs = properties.getSection(Gerrit.class).listenAs;
      if (!listenAs.isEmpty()) {
        userChangeWorker.addPublisher(publisher, listenAs);
      } else {
        defaultChangeWorker.addPublisher(publisher);
      }
      publisherList.add(publisher);
    }
  }

  @Override
  public void stop() {
    for (Publisher publisher : publisherList) {
      publisher.stop();
      String listenAs = publisher.getProperties().getSection(Gerrit.class).listenAs;
      if (!listenAs.isEmpty()) {
        userChangeWorker.removePublisher(publisher);
      } else {
        defaultChangeWorker.removePublisher(publisher);
      }
    }
    publisherList.clear();
  }

  private List<Properties> load() {
    List<Properties> propList = new ArrayList<>();
    // Load base
    Properties base = propFactory.create(pluginDataDir.resolve(pluginName + FILE_EXT));
    base.load();

    // Load site
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(pluginDataDir.resolve(SITE_DIR), "*" + FILE_EXT)) {
      for (Path configFile : ds) {
        Properties site = propFactory.create(configFile);
        if (site.load(base)) {
          propList.add(site);
        }
      }
    } catch (IOException iex) {
      LOGGER.warn(iex.getMessage());
    }
    return propList;
  }
}
