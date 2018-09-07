---
title: HttpPlainText
category: clients
caption: HttpPlainText
feature:
  artifact: io.ktor:ktor-client-core:$ktor_version
  class: io.ktor.client.features.HttpPlainText
---

This feature processes the request content as plain text of a specified charset by `defaultCharset`.
Also, it will process the response content as plain text too.

{% include feature.html %}

## Install

```kotlin
val client = HttpClient(HttpClientEngine) {
    install(HttpPlainText) {
        defaultCharset = Charsets.UTF_8
    }
}
```

Bear in mind that the default charset is the JVM's charset that could be different between systems. \\
That's why it is recommended to specify the default charset.
{: .note}
