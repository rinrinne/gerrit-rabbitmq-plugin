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
package com.googlesource.gerrit.plugins.rabbitmq.solver;

import com.google.gerrit.extensions.annotations.PluginData;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.SitePaths;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.rabbitmq.solver.version.V1;

import java.io.File;

public class SolverImpl implements Solver {

  private final Solver solver;

  @Inject
  public SolverImpl(
      @PluginName final String pluginName,
      @PluginData final File pluginData,
      final SitePaths sites
      ) {
    this.solver = new V1(pluginName, pluginData, sites);
  }

  public void solve() {
    solver.solve();
  }
}
