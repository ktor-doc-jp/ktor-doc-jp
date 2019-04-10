---
title: Json
category: clients
caption: Json
feature:
  artifact: io.ktor:ktor-client-json:$ktor_version
  class: io.ktor.client.features.json.JsonFeature
ktor_version_review: 1.2.0
---

Processes the request and the response payload as JSON, serializing
and de-serializing them using a specific `serializer: JsonSerializer`.

```kotlin
val client = HttpClient(HttpClientEngine) {
    install(JsonFeature)
}
```

You have a [full example using JSON](/clients/http-client/examples.html#example-json).

{% include feature.html %}

## Serializers

The `JsonFeature` has a default serializer(implicitly obtained or by calling `defaultSerializer()`)
based on a ServiceLoader on JVM(supporting Gson or Jackson depending on the artifact included),
and a serializer based on [kotlinx.serialization](/kotlinx/serialization.html) for Native as well as for JavaScript.

You can also get the default serializer by calling `io.ktor.client.features.json.defaultSerializer()`

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

### Kotlinx.Serialization

{: #kotlinx-serialization }

```kotlin
val client = HttpClient(HttpClientEngine) {
    install(JsonFeature) {
        serializer = KotlinxSerializer()
    }
}
```

To use this feature, you need to include `io.ktor:ktor-client-json-jvm` artifact on the jvm.
{: .note.artifact }
