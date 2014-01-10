gerrit-rabbitmq-plugin: Gerrit event publish plugin via RabbitMQ
=======================

* Author: rinrinne a.k.a. rin_ne
* Repository: http://github.com/rinrinne/gerrit-rabbitmq-plugin

Synopsis
----------------------

This is Gerrit plugin.

This can publish gerrit events to message queue provided by RabbitMQ.
Published events are the same as Gerrit stream evnets.

Now available for Gerrit 2.8 only, but you may use this on another version of Gerrit if you modify `Gerrit-ApiVersion` in pom.xml.

Minimum configuration
----------------------

In `rabbitmq.config`

```
[amqp]
  uri = amqp://localhost
```

You should declare exchange named `gerrit.publish` beforehand.

License
---------------------

Apache 2.0 License

Copyright
---------------------

Copyright (c) 2013 rinrinne a.k.a. rin_ne
