---
title: Call Logging
caption: Log the client requests
category: servers
permalink: /servers/features/call-logging.html
feature:
  artifact: io.ktor:ktor-server-core:$ktor_version
  class: io.ktor.features.CallLogging
redirect_from:
- /features/call-logging.html
---

You might want to log client requests: and the Call Logging feature does just that.
It uses the `ApplicationEnvironment.log` (`LoggerFactory.getLogger("Application")`)
that uses slf4j so you can easily configure the output. For more information
on logging in Ktor, please check the [logging in the ktor](/servers/logging.html) page.

{% include feature.html %}

## Basic usage

The basic unconfigured feature logs every request using the level TRACE: 

```kotlin
install(CallLogging)
```

## Configuring

This feature allows you to configure the log level and filtering the requests that are being logged:

```kotlin
install(CallLogging) {
    level = Level.INFO
    filter { call -> call.request.path().startsWith("/section1") }
    filter { call -> call.request.path().startsWith("/section2") }
    // ...
}
```

The filter method keeps a whitelist list of filters. If no filters are defined,
everything is logged. And if there are filters, if any of them returns true,
the call will be logged.

In the example, it will log both: `/section1/*` and `/section2/*` requests.