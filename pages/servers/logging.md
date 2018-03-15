---
title: Logging
caption: Logging in Ktor
section: Servers
permalink: /servers/logging.html
keywords: SLF4J logback log4j
---

Ktor uses [SLF4J](https://www.slf4j.org/) for logging.
## Accessing the main logger

The `ApplicationEnvironment` interface has a `log` property.
You can access it inside an `ApplicationCall` with `call.application.environment.log`.

## SLF4J Providers

If you don't add a logging provider, you will see the
following message when you run your application:

```
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
```

### Logback provider

You can use [logback](https://logback.qos.ch/), which is the successor of log4j, as SLF4J provider:

```groovy
compile "ch.qos.logback:logback-classic:1.2.1"
```

### Configuring the Logback provider

If the default logging is not enough, you can put a `logback.xml` or (`logback-test.xml` that has higher priority) file in your `resources` folder
to adjust logging if it is not being useful to you. For example:

```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{YYYY-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="trace">
        <appender-ref ref="STDOUT"/>
    </root>

    <logger name="org.eclipse.jetty" level="INFO"/>
    <logger name="io.netty" level="INFO"/>
</configuration>
```
