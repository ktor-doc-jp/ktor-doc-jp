---
title: HttpRedirect
category: clients
caption: HttpRedirect 
feature:
  artifact: io.ktor:ktor-client-core:$ktor_version
  class: io.ktor.client.features.HttpRedirect
---

By default, Ktor HTTP client doesn't follow redirections (except for Apache and configuring it), this feature
allows to follow `Location` redirects in a way that works with any HTTP engine. Its usage is pretty straightforward
and the only configurable property is the `maxJumps` (20 by default) that limits how many redirects are tried
before giving up (to prevent infinite redirects).

{% include feature.html %}

## Install

```kotlin
val client = HttpClient(HttpClientEngine) {
    install(HttpRedirect) {
        maxJumps = 20
    }
}
``` 

This feature is included in the core of the HttpClient so it is available always along the client.
{: .note}
