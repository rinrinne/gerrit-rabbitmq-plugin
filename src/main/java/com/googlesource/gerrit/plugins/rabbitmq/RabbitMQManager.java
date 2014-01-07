package com.googlesource.gerrit.plugins.rabbitmq;

import com.google.gerrit.common.ChangeListener;
import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.gerrit.server.events.ChangeEvent;
import com.google.gson.Gson;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class RabbitMQManager implements ChangeListener, LifecycleListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQManager.class);
  private final static int MONITOR_FIRSTTIME_DELAY = 15000;
  private final Properties properties;
  private final AMQPSession session;
  private final Gson gson = new Gson();
  private final Timer monitorTimer = new Timer();

  @Inject
  public RabbitMQManager(Properties properties, AMQPSession session) {
    this.properties = properties;
    this.session = session;
  }

  @Override
  public void start() {
    session.connect();
    monitorTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        if (!session.isOpen()) {
          LOGGER.info("#start: try to reconnect");
          session.connect();
        }
      }
    }, MONITOR_FIRSTTIME_DELAY, properties.getConnectionMonitorInterval());
  }

  @Override
  public void stop() {
    monitorTimer.cancel();
    session.disconnect();
  }

  @Override
  public void onChangeEvent(ChangeEvent event) {
    session.sendMessage(gson.toJson(event));
  }

}
