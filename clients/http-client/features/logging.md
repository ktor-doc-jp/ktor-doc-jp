---
title: Logging
category: clients
caption: Logging
feature:
  artifact: io.ktor:ktor-client-logging:$ktor_version
  class: io.ktor.client.features.logging.Logging
---

This feature add multiplatform logging for http calls.

{% include feature.html %}

## Installation

```kotlin
val client = HttpClient() {
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.HEADERS
    }
}

```
