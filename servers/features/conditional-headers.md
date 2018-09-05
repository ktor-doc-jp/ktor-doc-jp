---
title: Conditional Headers
caption: Easy '304 Not Modified' Responses
category: servers
permalink: /servers/features/conditional-headers.html
keywords: etag last-modified
feature:
  artifact: io.ktor:ktor-server-core:$ktor_version
  class: io.ktor.features.ConditionalHeaders
redirect_from:
- /features/conditional-headers.html
---

ConditionalHeaders feature adds the ability to avoid sending content if the client already has the same content. It does so by
checking the `ETag` or `LastModified` properties of the `Resource` or `FinalContent` that are sent and comparing these 
properties to what client indicates it is having. If the conditions allow it, the entire content is not sent and a
"304 Not Modified" response is sent instead. 

```kotlin
install(ConditionalHeaders)
```

{% include feature.html %}

### Configuration

This feature doesn't have any configuration object, but a configuration script is executed if present.

### Extensibility

`Version` interface implementations are attached to the `Resource` instances, and you can return custom implementations
with your own logic. Please note that `FinalContent` is only checked for `ETag` and `LastModified` headers.