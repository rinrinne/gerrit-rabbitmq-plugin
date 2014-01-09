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
    durable = true
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
:   The URI of RabbitMQ server's endpoint. If not specified,
    defaults to "amqp://localhost".

amqp.username
:   Username for RabbitMQ connection authentication. If not
    specified, defaults to "guest".

amqp.password
:   Password for RabbitMQ connection authentication. If not
    specified, defaults to "guest".

queue.name
:   The name of queue. If not specified, defaults to "".

queue.declare
:   true if you want to declare queue on startup. If not specified, defaults to false.
    Also need to specify `queue.name`.

queue.durable
:   true if you want to declare a drable queue. If not specified, defaults to true.

queue.autoDelete
:   true if you want to declare an autodelete queue. If not specified, defaults to false.

queue.exclusive
:   true if you want to declare an exclusive queue. If not specified, defaults to false.

exchange.name
:   The name of exchange. If not specified, defaults to "gerrit.publish".

exchange.declare
:   true if you want to declare exchange on startup. If not specified, defaults to false.

exchange.type
:   The type of exchange. You can specify the following value:
     * "direct"
     * "fanout"
     * "topic"

exchange.durable
:   true if you want to declare a durable exchange. If not specified, defaults to true.

exchange.autoDelete
:   true if you want to declare an autodelete exchange. If not specified, defaults to false.

bind.startup
:   true if you want to bind queue to exchange on startup. If not specified, defaults to false.
    Also need to specify `queue.name`.

bind.routingKey
:   The name of routing key. This is used to bind queue to exchange. If not specified, defaults to "".

message.deliverMode
:   The delivery mode. if not specified, defaults to 1.
    * 1 - non-persistent
    * 2 - persistent

message.priority
:   The priority of message. if not specified, defaults to 0.

message.routingKey
:   The name of routingKey. This is stored to message property. If not specified, defaults to "".

gerrit.name
:   The name of gerrit(not hostname). This is your given name to identify your gerrit.
    If not specified, defaults to "".
    This can be used for message header only.

gerrit.hostname
:   The hostname of gerrit for SCM connection.
    If not specified, defaults to "".
    This can be used for message header only.

gerrit.scheme
:   The scheme of gerrit for SCM connection.
    If not specified, defaults to "ssh".
    This can be used for message header only.

gerrit.port
:   The port number of gerrit for SCM connection.
    If not specified, defaults to 29418.
    This can be used for message header only.

monitor.interval
:   The interval time in milliseconds for connection monitor.
    If not specified, defaults to 15000.
    You can specify the value more than 5000.