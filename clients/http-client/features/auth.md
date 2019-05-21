---
title: Auth
category: clients
caption: Auth
feature:
  artifact: io.ktor:ktor-client-auth:$ktor_version
  class: io.ktor.client.features.auth.Auth
keywords: authentication
redirect_from:
- /clients/http-client/features/basic-auth.html
ktor_version_review: 1.2.0
---

Ktor client supports authentication out of the box as a standard pluggable feature:

{% include feature.html %}

## Installation

``` kotlin
val client = HttpClient() {
    install(Auth) {
        // providers config
        ...
    }
}
```

## Providers

### Basic

This provider sends an `Authorization: Basic` with the specified credentials:

```kotlin
val client = HttpClient() {
    install(Auth) {
        basic {
            username = "username"
            password = "password"
        }
    }
}
```

This feature implements the IETF's [RFC 7617](https://tools.ietf.org/html/rfc7617).

### Digest

This provider sends an `Authorization: Digest` with the specified credentials:

```kotlin
val client = HttpClient() {
    install(Auth) {
        digest {
            username = "username"
            password = "password"
            realm = "custom"
        }
    }
}
```

This feature implements the IETF's [RFC 2617](https://tools.ietf.org/html/rfc2617).
