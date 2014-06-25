gerrit-rabbitmq-plugin: Gerrit event publish plugin via RabbitMQ
=======================

* Author: rinrinne a.k.a. rin_ne
* Repository: http://github.com/rinrinne/gerrit-rabbitmq-plugin
* Release: http://github.com/rinrinne/gerrit-rabbitmq-plugin/releases

[![Build Status](https://travis-ci.org/rinrinne/gerrit-rabbitmq-plugin.png?branch=master)](https://travis-ci.org/rinrinne/gerrit-rabbitmq-plugin)

Synopsis
----------------------

This is Gerrit plugin.

This can publish gerrit events to message queue provided by RabbitMQ.
Published events are the same as Gerrit stream evnets.

This plugin works on Gerrit 2.8 or later.

Environments
---------------------

* `linux`
  * `java-1.7`
    * `gradle`

Build
---------------------

To build plugin with gradle.

    ./gradlew build

Using another version API
--------------------------

Now avaliable for Gerrit 2.8.5 only. If you want to use it on another version of Gerrit, please try the below.

    ./gradlew build -PapiVersion=2.8

Reference
---------------------

* [Configuration]
* [Message Format]

[Configuration]: https://github.com/rinrinne/gerrit-rabbitmq-plugin/blob/master/src/main/resources/Documentation/config.md
[Message Format]: https://github.com/rinrinne/gerrit-rabbitmq-plugin/blob/master/src/main/resources/Documentation/message.md

Minimum Configuration
---------------------

```
  [amqp]
    uri = amqp://localhost
  [exchange]
    name = exchange-for-gerrit-queue
  [message]
    routingKey = com.foobar.www.gerrit
  [gerrit]
    name = foobar-gerrit
    hostname = www.foobar.com
```

History
---------------------

* 1.4
  * Binary release
  * Add gradle support
  * Remove maven support

* 1.3
  * Build with Buck
  * Bumped api version to 2.8.3

* 1.2
  * Fix repository location for gerrit-api
  * Update README

* 1.1
  * Fix channel handling
  * Add property: `monitor.failureCount`
  * Update README and documents 

* 1.0
  *  First release

License
---------------------

The Apache Software License, Version 2.0

Copyright
---------------------

Copyright (c) 2013 rinrinne a.k.a. rin_ne
