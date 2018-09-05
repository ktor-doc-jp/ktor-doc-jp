---
title: JsonFeature
category: clients
caption: JsonFeature
---

Processes the request and the response payload as JSON, serializing
and de-serializing them using a specific `serializer: JsonSerializer`.

```kotlin
val client = HttpClient(HttpClientEngine) {
    install(JsonFeature)
}
```

You have a [full example using JSON](/clients/http-client/examples.html#example-json).

To use this feature, you need to include `io.ktor:ktor-client-json` artifact.
{: .note.artifact }

## Serializers

The `JsonFeature` has a default serializer based on a ServiceLoader on JVM,
and a serializer based on [kotlinx.serialization](/kotlinx/serialization.html) for Native.

### Gson
{: #gson }

```kotlin
val client = HttpClient(HttpClientEngine) {
    install(JsonFeature) {
        serializer = GsonSerializer()
    }
}
```

To use this feature, you need to include `io.ktor:ktor-client-gson` artifact.
{: .note.artifact }

### Jackson
{: #jackson }

```kotlin
val client = HttpClient(HttpClientEngine) {
    install(JsonFeature) {
        serializer = JacksonSerializer()
    }
}
```

To use this feature, you need to include `io.ktor:ktor-client-jackson` artifact.
{: .note.artifact }
