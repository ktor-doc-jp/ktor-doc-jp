---
title: User Agent
category: clients
caption: User Agent
feature:
  artifact: io.ktor:ktor-client-core:$ktor_version
  class: io.ktor.client.features.UserAgent
ktor_version_review: 1.2.0
---

User-Agent をリクエストヘッダに追加する機能を提供しています。

{% include feature.html %}

## インストール

```kotlin
val client = HttpClient() {

    // Full configuration.
    install(UserAgent) {
        agent = "ktor"
    }

    // Shortcut for the browser-like user agent.
    BrowserUserAgent()

    // Shortcut for the curl-like user agent.
    CurlUserAgent()
}

```
