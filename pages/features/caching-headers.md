---
title: Caching Headers
caption: Controlling cache headers
section: Features
permalink: /features/caching-headers.html
feature:
    artifact: io.ktor:ktor-server-core:$ktor_version
    class: io.ktor.features.CachingHeaders
---

The CachingOptions feature adds the ability to send the headers `Cache-Control` and `Expires`
used by clients and proxies to cache requests in an easy way.

{% include feature.html %}

The basic feature is installed just like many others, but for it to do something, you have to define
`options` blocks transforming outputContent to CachingOptions using for example 

```kotlin
install(CachingHeaders)
```

This feature adds an `options` configuration method that allows you to define code
to optionally select a `CachingOptions` from a provided `outgoingContent: OutgoingContent`.
You can, for example, use the `Content-Type` of the outgoing message to determine which
Cache-Control to use: 

```kotlin
install(CachingHeaders) {
    options { outgoingContent ->
        when (outgoingContent.contentType?.withoutParameters()) {
            ContentType.Text.CSS -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60))
            else -> null
        }
    }
}
```
