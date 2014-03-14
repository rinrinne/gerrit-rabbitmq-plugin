include_defs('//lib/maven.defs')

gerrit_plugin(
  name = 'rabbitmq',
  srcs = glob(['src/main/java/**/*.java']),
  resources = glob(['src/main/resources/**/*']),
  manifest_entries = [
    'Implementation-Title: Rabbitmq plugin',
    'Implementation-URL: https://github.com/rinrinne/gerrit-rabbitmq-plugin',
    'Gerrit-PluginName: rabbitmq',
    'Gerrit-Module: com.googlesource.gerrit.plugins.rabbitmq.Module',
  ],
  compile_deps = [
    '//lib/commons:lang',
    '//lib/commons:codec',
    '//lib:gson',
  ],
  deps = [
    ':rabbitmq-amqp-client',
  ],
)

maven_jar(
  name = 'rabbitmq-amqp-client',
  id = 'com.rabbitmq:amqp-client:3.2.2',
  license = 'MPL1.1',
)
