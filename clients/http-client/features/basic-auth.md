---
title: BasicAuth
category: clients
caption: BasicAuth 
feature:
  artifact: io.ktor:ktor-client-auth-basic:$ktor_version
  class: io.ktor.client.features.auth.basic.BasicAuth
---

This feature sends an `Authorization: Basic` with the specified credentials:

{% include feature.html %}

## Install

```kotlin
val client = HttpClient(HttpClientEngine) {
    install(BasicAuth) {
        username = "username"
        password = "password"
    }
}
```

This feature implements the IETF's [RFC 7617](https://tools.ietf.org/html/rfc7617).
{: .note}
