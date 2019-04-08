---
title: Logging
category: clients
caption: Logging
feature:
  artifact: io.ktor:ktor-client-logging:$ktor_version
  class: io.ktor.client.features.logging.Logging
ktor_version_review: 1.2.0
---

This feature adds multiplatform logging for HTTP calls.

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
