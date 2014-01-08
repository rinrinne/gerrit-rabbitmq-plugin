RabbitMQ Configuration
======================

Some parameters can be configured in the plugin config file `rabbitmq.config`.

```
  [amqp]
    uri = amqp://www.foobar.com:5672
    username = guest
    password = guest
    queue = gerrit-queue
    exchange = direct-gerrit-exchange
    routingKey = com.foobar.www.gerrit
  [gerrit]
    name = foobar-gerrit
    hostname = www.foobar.com
    scheme = ssh
    port = 24918
  [message]
    deliveryMode = 1
    priority = 0
  [monitor]
    interval = 15000
```

amqp.ri
:   The URI of RabbitMQ server's endpoint. If not specified,
    defaults to "amqp://localhost".

amqp.username
:   Username for RabbitMQ connection authentication. If not
    specified, defaults to "guest".

amqp.password
:   Password for RabbitMQ connection authentication. If not
    specified, defaults to "guest".

amqp.queue
:   The name of queue. If specified, this queue is declared to RabbitMQ.
    Also the unique exchange is declated with `direct` type (or `fanout`
    type if `amqp.routingKey` is not specified). Then bind queue
    from this exchange.
    +
    Note that `amqp.exchange` is ignored.

amqp.exchange
:   The name of exchange. This is used when `amqp.queue` is not specified.
    The named exchange is not created. It means that it would be failure
    if named exchange is not exist in RabbitMQ.

amqp.routingKey
:   The name of routing key. if not specified, defaults to the same as plugin name.

gerrit.mame
:   The name of gerrit(not hostname). This is your given name to identify your gerrit.
    This can be used for message header only.

gerrit.hostname
:   The hostname of gerrit for SCM connection.
    This can be used for message header only.

gerrit.scheme
:   The scheme of gerrit for SCM connection.
    If not specified, defaults to "ssh".
    This can be used for message header only.

gerrit.port
:   The port number of gerrit for SCM connection.
    If not specified, defaults to 29418.
    This can be used for message header only.

message.deliverMode
:   The delivery mode. if not specified, defaults to 1.
    * 1 - non-persistent
    * 2 - persistent

message.priority
:   The priority of message. if not specified, defaults to 0.

monitor.interval
:   The interval time in milliseconds for connection monitor.
    If not specified, defaults to 15000.
    You can specify the value more than 5000.