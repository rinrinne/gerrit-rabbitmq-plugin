package com.googlesource.gerrit.plugins.rabbitmq;

public enum Keys {
  AMQP_URI("amqp.uri", null),
  AMQP_USERNAME("amqp.username", null),
  AMQP_PASSWORD("amqp.password", null),
  AMQP_QUEUE("amqp.queue", null),
  AMQP_EXCHANGE("amqp.exchange", null),
  AMQP_ROUTINGKEY("amqp.routingKey", null),
  MESSAGE_DELIVERY_MODE("message.deliveryMode", null),
  MESSAGE_PRIORITY("message.priority", null),
  GERRIT_NAME("gerrit.name", "gerrit-name"),
  GERRIT_HOSTNAME("gerrit.hostname", "gerrit-host"),
  GERRIT_SCHEME("gerrit.scheme", "gerrit-scheme"),
  GERRIT_PORT("gerrit.port", "gerrit-port"),
  GERRIT_FRONT_URL("gerrit.canonicalWebUrl", "gerrit-front-url"),
  GERRIT_VERSION("gerrit.version", "gerrit-version"),
  CONNECTION_MONITOR_INTERVAL("monitor.interval", null);

  public String section;
  public String value;
  public String header;

  Keys(String property, String header) {
    String[] part = property.split("\\.");
    this.section = part[0];
    this.value = part[1];
    this.header = header;
  }
}
