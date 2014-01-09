Gerrit plugin: RabbitMQ
=======================

This plugin can publish gerrit events to message queue provided by RabbitMQ.

Now this plugin is available for Gerrit 2.8 only, but you may use this on another version of Gerrit if you modify `Gerrit-ApiVersion` in pom.xml.

Minimum configuration
=======================

In `rabbitmq.config`

```
[amqp]
  uri = amqp://localhost
```

You should declare exchange named `gerrit.publish` beforehand.
