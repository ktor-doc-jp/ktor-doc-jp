---
title: HttpCookies
category: clients
caption: HttpCookies 
feature:
  artifact: io.ktor:ktor-client-core:$ktor_version
  class: io.ktor.client.features.DefaultRequest
  method: io.ktor.client.features.defaultRequest
---

This feature keeps cookies between calls or forces specific cookies.

{% include feature.html %}

## Installation

```kotlin
val client = HttpClient(HttpClientEngine) {
    install(HttpCookies) {
        // Will keep an in-memory map with all the cookies from previous requests.
        storage = AcceptAllCookiesStorage()
        
        // Will ignore Set-Cookie and will send the specified cookies.
        storage = ConstantCookieStorage(Cookie("mycookie1", "value"), Cookie("mycookie2", "value"))
    }
}
client.cookies("mydomain.com")
```
