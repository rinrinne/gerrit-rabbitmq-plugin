package com.googlesource.gerrit.plugins.rabbitmq;

import com.google.gerrit.common.ChangeListener;
import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.gerrit.server.events.ChangeEvent;
import com.google.gson.Gson;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginChangeListener implements ChangeListener, LifecycleListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(PluginChangeListener.class);
  private final AMQPSession.Factory factory;
  private AMQPSession session;
  private final Gson gson = new Gson();

  @Inject
  public PluginChangeListener(AMQPSession.Factory factory) {
    this.factory = factory;
  }

  @Override
  public void start() {
    session = factory.create();
    session.connect();
  }

  @Override
  public void stop() {
    if (session != null) {
      session.disconnect();
    }
    session = null;
  }

  @Override
  public void onChangeEvent(ChangeEvent event) {
    session.sendMessage(gson.toJson(event));
  }

}
