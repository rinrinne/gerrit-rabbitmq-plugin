package com.googlesource.gerrit.plugins.rabbitmq.worker;

import com.googlesource.gerrit.plugins.rabbitmq.message.Publisher;

public interface EventWorker {
  public void addPublisher(Publisher publisher);
  public void addPublisher(Publisher publisher, String userName);
  public void removePublisher(Publisher publisher);
  public void clear();
}
