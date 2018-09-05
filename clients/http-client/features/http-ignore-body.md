---
title: HttpIgnoreBody
category: clients
caption: HttpIgnoreBody 
---

This feature discards the body of the response:

```kotlin
val client = HttpClient(HttpClientEngine) {
    install(HttpIgnoreBody)
}
```

Use this if you are only interested in the response headers, and you cannot use the HEAD verb.
This will use less memory and will execute faster.
{: .note}
