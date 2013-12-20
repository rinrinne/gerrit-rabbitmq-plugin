package com.googlesource.gerrit.plugins.rabbitmq;

public enum Keys {
  AMQP_URI("amqpUri", null),
  AMQP_USERNAME("amqpUsername", null),
  AMQP_PASSWORD("amqpPassword", null),
  AMQP_QUEUE("amqpQueue", null),
  AMQP_EXCHANGE("amqpExchange", null),
  AMQP_ROUTINGKEY("amqpRoutingKey", null),
  GERRIT_NAME("name", "gerrit-name"),
  GERRIT_HOSTNAME("sshHostname", "gerrit-host"),
  GERRIT_SCHEME(null, "gerrit-scheme"),
  GERRIT_PORT("sshPort", "gerrit-port"),
  GERRIT_FRONT_URL("canonicalWebUrl", "gerrit-front-url"),
  GERRIT_VERSION(null, "gerrit-version");

  public String property;
  public String header;

  Keys(String property, String header) {
    this.property = property;
    this.header = header;
  }
}
