package com.googlesource.gerrit.plugins.rabbitmq.config;

import com.googlesource.gerrit.plugins.rabbitmq.config.section.Section;

import org.eclipse.jgit.lib.Config;

import java.nio.file.Path;
import java.util.Set;

public interface Properties extends Cloneable {
  public Config toConfig();
  public boolean load();
  public boolean load(Properties baseProperties);
  public Path getPath();
  public String getName();
  public Set<Section> getSections();
  public <T extends Section> T getSection(Class<T> clazz);
  public AMQProperties getAMQProperties();
}
