package com.googlesource.gerrit.plugins.rabbitmq;

import com.google.inject.Inject;

import java.util.TimerTask;

public class ConnectionMonitorTask extends TimerTask {

  private final AMQPSession.Factory sessionFactory;

  @Inject
  public ConnectionMonitorTask(AMQPSession.Factory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void run() {
    AMQPSession session = sessionFactory.create();
    if (!session.isOpen()) {
      session.connect();
    }
  }

}
