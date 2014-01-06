RabbitMQ Configuration
======================

```
  [plugin "rabbimq"]
    amqpUri = amqp://www.foobar.com:5672
    amqpUsername = bob
    amqpPassword = bobpw
    amqpQueue = gerrit-queue
    amqpExchange = gerrit-exchange
    amqpRoutingKey = com.foobar.www.gerrit
    gerritName = foobar-gerrit
    gerritHostname = www.foobar.com
    gerritPort = 24918
    monitorInterval = 15000
```
