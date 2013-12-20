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
    name = foobar-gerrit
    sshHostname = www.foobar.com
    sshPort = 24918
```
