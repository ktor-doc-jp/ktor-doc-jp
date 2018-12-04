---
title: UserAgent
category: clients
caption: UserAgent 
feature:
  artifact: io.ktor:ktor-client-core:$ktor_version
  class: io.ktor.client.features.UserAgent
---

This feature add User-Agent header to requests.

{% include feature.html %}

## Install

```kotlin
val client = HttpClient() {

    // full configuration
    install(UserAgent) {
        agent = "ktor"
    }

    // short version for browser-like user agent
    BrowserUserAgent()

    // short version for curl-like user agent
    CurlUserAgent()
}

```
