package com.googlesource.gerrit.plugins.rabbitmq.message;

import com.google.gerrit.common.ChangeListener;

import com.googlesource.gerrit.plugins.rabbitmq.config.Properties;
import com.googlesource.gerrit.plugins.rabbitmq.session.Session;

public interface Publisher extends ChangeListener {
  public void start();
  public void stop();
  public void enable();
  public void disable();
  public boolean isEnable();
  public Session getSession();
  public Properties getProperties();
  public String getName();
}
