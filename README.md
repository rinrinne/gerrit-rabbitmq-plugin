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

BUild
---------------------

    mvn package

License
---------------------

The Apache Software License, Version 2.0

Copyright
---------------------

Copyright (c) 2013 rinrinne a.k.a. rin_ne
