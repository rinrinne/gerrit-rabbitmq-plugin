RabbitMQ Configuration
======================

Some parameters can be configured in the plugin config file `rabbitmq.config`.

```
  [amqp]
    uri = amqp://localhost
    username = guest
    password = guest
  [queue]
    name = gerrit-queue
    decralation = false
    durable = true
    autoDelete = false
    exclusive = false
  [exchange]
    name = exchange-for-gerrit-queue
    declaration = false
    type = fanout
    durable = false
    autoDelete = false
  [bind]
    startUp = false
    routingKey = com.foobar.www.gerrit
  [message]
    deliveryMode = 1
    priority = 0
    routingKey = com.foobar.www.gerrit
  [gerrit]
    name = foobar-gerrit
    hostname = www.foobar.com
    scheme = ssh
    port = 24918
  [monitor]
    interval = 15000
```

amqp.uri
:   The URI of RabbitMQ server's endpoint.

amqp.username
:   Username for RabbitMQ connection authentication.

amqp.password
:   Password for RabbitMQ connection authentication.

queue.name
:   The name of queue.

queue.declare
:   true if you want to declare queue on startup.

queue.durable
:   true if you want to declare a drable queue.

queue.autoDelete
:   true if you want to declare an autodelete queue.

queue.exclusive
:   true if you want to declare an exclusive queue.

exchange.name
:   The name of exchange.

exchange.declare
:   true if you want to declare exchange on startup.

exchange.type
:   The type of exchange. You can specify the following value:
     * "direct"
     * "fanout"
     * "topic"

exchange.durable
:   true if you want to declare a durable exchange.

exchange.autoDelete
:   true if you want to declare an autodelete exchange.

bind.startUp
:   true if you want to bind queue to exchange on startup.
    Also need to specify `queue.name`.

bind.routingKey
:   The name of routing key. This is used to bind queue to exchange.

message.deliverMode
:   The delivery mode. if not specified, defaults to 1.
    * 1 - non-persistent
    * 2 - persistent

message.priority
:   The priority of message. if not specified, defaults to 0.

message.routingKey
:   The name of routingKey. This is stored to message property.

gerrit.name
:   The name of gerrit(not hostname). This is your given name to identify your gerrit.
    This can be used for message header only.

gerrit.hostname
:   The hostname of gerrit for SCM connection.
    This can be used for message header only.

gerrit.scheme
:   The scheme of gerrit for SCM connection.
    This can be used for message header only.

gerrit.port
:   The port number of gerrit for SCM connection.
    This can be used for message header only.

monitor.interval
:   The interval time in milliseconds for connection monitor.
    You can specify the value more than 5000.

Default Values
-----------------

You can change the below values by specifying them in `rabbitmq.config`.

**Bold** is String value.

|name                | value
|:-------------------|:------------------
|amqp.uri            | **amqp://localhost**
|amqp.username       | **guest**
|amqp.password       | **guest**
|queue.name          | **gerrit.events**
|queue.declare       | false
|queue.durable       | true
|queue.autoDelete    | false
|queue.exclusive     | false
|exchange.name       | **gerrit.publish**
|exchange.declare    | false
|exchange.type       | **fanout**
|exchange.durable    | false
|exchange.autoDelete | false
|bind.startUp        | false
|bind.routingKey     | *Empty*
|message.deliverMode | 1
|message.priority    | 0
|message.routingKey  | *Empty*
|gerrit.name         | *Empty*
|gerrit.hostname     | *Empty*
|gerrit.scheme       | **ssh**
|gerrit.port         | 29418
|monitor.interval    | 15000
