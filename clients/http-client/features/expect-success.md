---
title: ExpectSuccess
category: clients
caption: ExpectSuccess 
feature:
  artifact: io.ktor:ktor-client-core:$ktor_version
  class: io.ktor.client.features.ExpectSuccess
---

This feature checks that the response is OK (code < 300) or throws a `BadResponseStatus`. 

{% include feature.html %}

## Install

This feature is installed by default.

## Prevent installing

You can prevent installing this feature by setting the `HttpClient.expectSuccess` property to `false`:

```kotlin
val client = HttpClient(HttpClientEngine) {
    expectSuccess = false
}
```
