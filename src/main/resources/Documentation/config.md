RabbitMQ Configuration
======================

Some parameters can be configured using config file.

Directory
---------------------

You can locate config files to `$site_path/data/rabbitmq/site`.
File extension must be `.config`.
Connection to RabbitMQ will be established for each files.

If `rabbitmq.config` exists in `$site_path/data/rabbitmq`, it is loaded at first.
It means that this is default for all config files.

File format
---------------------

```
  [amqp]
    uri = amqp://localhost
    username = guest
    password = guest
  [exchange]
    name = exchange-for-gerrit-queue
  [message]
    deliveryMode = 1
    priority = 0
    routingKey = com.foobar.www.gerrit
  [gerrit]
    name = foobar-gerrit
    hostname = www.foobar.com
    scheme = ssh
    port = 29418
    listenAs = gerrituser
  [monitor]
    interval = 15000
    failureCount = 15
```

* `amqp.uri`
    * The URI of RabbitMQ server's endpoint.

* `amqp.username`
    * Username for RabbitMQ connection authentication.

* `amqp.password`
    * Password for RabbitMQ connection authentication.

* `exchange.name`
    * The name of exchange.

* `message.deliveryMode`
    * The delivery mode. if not specified, defaults to 1.
        * 1 - non-persistent
        * 2 - persistent

* `message.priority`
    * The priority of message. if not specified, defaults to 0.

* `message.routingKey`
    * The name of routingKey. This is stored to message property.

* `gerrit.name`
    * The name of gerrit(not hostname). This is your given name to identify your gerrit.
      This can be used for message header only.

* `gerrit.hostname`
    * The hostname of gerrit for SCM connection.
      This can be used for message header only.

* `gerrit.scheme`
    * The scheme of gerrit for SCM connection.
      This can be used for message header only.

* `gerrit.port`
    * The port number of gerrit for SCM connection.
      This can be used for message header only.

* `gerrit.listenAs`
    * The user of gerrit who listen events.
      If not specified, listen events as unrestricted user.

* `monitor.interval`
    * The interval time in milliseconds for connection monitor.
      You can specify the value more than 5000.

* `monitor.failureCount`
    * The count of failure. If the command for publishing message failed in the specified number of times
      in succession, connection will be renewed.

Default Values
-----------------

You can change the below values by specifying them in config file.

**Bold** is String value.

|name                 | value
|:--------------------|:------------------
|amqp.uri             | **amqp://localhost**
|amqp.username        | **guest**
|amqp.password        | **guest**
|exchange.name        | **gerrit.publish**
|message.deliveryMode | 1
|message.priority     | 0
|message.routingKey   | *Empty*
|gerrit.name          | *Empty*
|gerrit.hostname      | *Empty*
|gerrit.scheme        | **ssh**
|gerrit.port          | 29418
|gerrit.listenAs      | *Unrestricted user*
|monitor.interval     | 15000
|monitor.failureCount | 15
