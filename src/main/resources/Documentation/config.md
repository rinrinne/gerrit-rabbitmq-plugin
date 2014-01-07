RabbitMQ Configuration
======================

Some parameters can be configured in the standard Gerrit config file `gerrit.config`.

```
  [plugin "rabbimq"]
    amqpUri = amqp://www.foobar.com:5672
    amqpUsername = guest
    amqpPassword = guest
    amqpQueue = gerrit-queue
    amqpExchange = gerrit-exchange
    amqpRoutingKey = com.foobar.www.gerrit
    gerritName = foobar-gerrit
    gerritHostname = www.foobar.com
    gerritScheme = ssh
    gerritPort = 24918
    messageDeliveryMode = 1
    messagePriority = 0
    monitorInterval = 15000
```

rabbitmq.amqpUri
:   The URI of RabbitMQ server's endpoint. **this is mandatory**.

rabbitmq.amqpUsername
:   Username for RabbitMQ connection authentication. If not
    specified, defaults to "guest".

rabbitmq.amqpPassword
:   Password for RabbitMQ connection authentication. If not
    specified, defaults to "guest".

rabbitmq.amqpQueue
:   The name of queue. If specified, this queue is declared to RabbitMQ.
    Also the unique exchange is declated with `direct` type (or `fanout`
    type if `rabbitmq.amqpRoutingKey` is not specified). Then bind queue
    from this exchange.
    +
    Note that `rabbitmq.amqpExchange` is ignored.

rabbitmq.amqpExchange
:   The name of exchange. This is used when `rabbitmq.amqpQueue` is not specified.
    The named exchange is not created. It means that it would be failure
    if named exchange is not exist in RabbitMQ.

rabbitmq.amqpRoutingKey
:   The name of routing key. if not specified, defaults to the same as plugin name.

rabbitmq.gerritName
:   The name of gerrit(not hostname). This is your given name to identify your gerrit.
    This can be used for message header only.

rabbitmq.gerritHostname
:   The hostname of gerrit for SCM connection.
    This can be used for message header only.

rabbitmq.gerritScheme
:   The scheme of gerrit for SCM connection.
    If not specified, defaults to "ssh".
    This can be used for message header only.

rabbitmq.gerritPort
:   The port number of gerrit for SCM connection.
    If not specified, defaults to 29418.
    This can be used for message header only.

rabbitmq.messageDeliverMode
:   The delivery mode. if not specified, defaults to 1.
    * 1 - non-persistent
    * 2 - persistent

rabbitmq.messagePriority
:   The priority of message. if not specified, defaults to 0.

rabbitmq.monitorInterval
:   The interval time in milliseconds for connection monitor.
    If not specified, defaults to 15000.
    You can specify the value more than 5000.