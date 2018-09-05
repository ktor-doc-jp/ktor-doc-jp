---
title: BasicAuth
category: clients
caption: BasicAuth 
---

### BasicAuth

This feature sends an `Authorization: Basic` with the specified credentials:

```kotlin
val client = HttpClient(HttpClientEngine) {
    install(BasicAuth) {
        username = "username"
        password = "password"
    }
}
```

To use this feature, you need to include the `ktor-client-auth-basic` artifact.
{: .note.artifact }

This feature implements the IETF's [RFC 7617](https://tools.ietf.org/html/rfc7617).
{: .note}
