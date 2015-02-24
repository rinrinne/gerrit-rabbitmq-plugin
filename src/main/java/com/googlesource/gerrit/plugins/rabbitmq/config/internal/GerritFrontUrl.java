package com.googlesource.gerrit.plugins.rabbitmq.config.internal;

import org.eclipse.jgit.lib.Config;

public interface GerritFrontUrl {
  public void setGerritFrontUrlFromConfig(Config config);
}
