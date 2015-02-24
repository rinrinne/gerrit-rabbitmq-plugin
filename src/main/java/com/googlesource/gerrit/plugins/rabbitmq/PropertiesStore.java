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

import com.google.gerrit.extensions.annotations.PluginData;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.googlesource.gerrit.plugins.rabbitmq.config.Properties;
import com.googlesource.gerrit.plugins.rabbitmq.config.internal.GerritFrontUrl;

import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class PropertiesStore extends AbstractList<Properties> {

  public static final String FILE_EXT = ".config";
  public static final String SITE_DIR = "site";

  private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesStore.class);


  private final List<Properties> propertiesStore;
  private final String pluginName;
  private final Path pluginDataDir;
  private final Config serverConfig;

  @Inject
  public PropertiesStore(
      @PluginName final String pluginName,
      @PluginData final File pluginData,
      @GerritServerConfig final Config serverConfig) {
    this.propertiesStore = new ArrayList<>();
    this.pluginName = pluginName;
    this.pluginDataDir = pluginData.toPath();
    this.serverConfig = serverConfig;
  }

  public void load() {
    // Load base
    Properties base = new Properties(pluginDataDir.resolve(pluginName + FILE_EXT));
    base.load();
    ((GerritFrontUrl)base).setGerritFrontUrlFromConfig(serverConfig);

    // Load site
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(pluginDataDir.resolve(SITE_DIR), "*" + FILE_EXT)) {
      for (Path configFile : ds) {
        Properties site = new Properties(configFile);
        if (site.load(base)) {
          propertiesStore.add(site);
        }
      }
    } catch (IOException iex) {
      LOGGER.warn(iex.getMessage());
    }
  }

  @Override
  public Properties get(int index) {
    return propertiesStore.get(index);
  }

  public Properties get(String name) {
    for (Properties p : propertiesStore) {
      if (p.getName().equals(name)) {
        return p;
      }
    }
    return null;
  }

  @Override
  public int size() {
    return propertiesStore.size();
  }
}
