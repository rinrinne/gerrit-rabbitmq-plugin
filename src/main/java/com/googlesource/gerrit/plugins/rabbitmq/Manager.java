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
import com.googlesource.gerrit.plugins.rabbitmq.worker.EventWorker;
import com.googlesource.gerrit.plugins.rabbitmq.worker.EventWorkerFactory;
import com.googlesource.gerrit.plugins.rabbitmq.worker.DefaultEventWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Singleton
public class Manager implements LifecycleListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(Manager.class);

  public static final String FILE_EXT = ".config";
  public static final String SITE_DIR = "site";

  private final String pluginName;
  private final Path pluginDataDir;
  private final EventWorker defaultEventWorker;
  private final EventWorker userEventWorker;
  private final PublisherFactory publisherFactory;
  private final PropertiesFactory propFactory;
  private final Set<Solver> solvers;
  private final List<Publisher> publisherList = new ArrayList<>();

  @Inject
  public Manager(
      @PluginName final String pluginName,
      @PluginData final File pluginData,
      final DefaultEventWorker defaultEventWorker,
      final EventWorkerFactory eventWorkerFactory,
      final PublisherFactory publisherFactory,
      final PropertiesFactory propFactory,
      final Set<Solver> solvers) {
    this.pluginName = pluginName;
    this.pluginDataDir = pluginData.toPath();
    this.defaultEventWorker = defaultEventWorker;
    this.userEventWorker = eventWorkerFactory.create();
    this.publisherFactory = publisherFactory;
    this.propFactory = propFactory;
    this.solvers = solvers;
  }

  @Override
  public void start() {
    for (Solver solver : solvers) {
      solver.solve();
    }

    List<Properties> propList = load();
    for (Properties properties : propList) {
      Publisher publisher = publisherFactory.create(properties);
      publisher.start();
      String listenAs = properties.getSection(Gerrit.class).listenAs;
      if (!listenAs.isEmpty()) {
        userEventWorker.addPublisher(publisher, listenAs);
      } else {
        defaultEventWorker.addPublisher(publisher);
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
        userEventWorker.removePublisher(publisher);
      } else {
        defaultEventWorker.removePublisher(publisher);
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
