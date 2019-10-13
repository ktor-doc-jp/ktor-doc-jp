---
title: JSON
category: clients
caption: JSON
feature:
  artifact: io.ktor:ktor-client-json:$ktor_version
  class: io.ktor.client.features.json.JsonFeature
ktor_version_review: 1.2.0
---

JSONとしてリクエスト・レスポンスのPayloadを処理するとき、特定の`serializer: JsonSerializer`を使ってシリアライズ・デシリアライズすることができます。

```kotlin
val client = HttpClient(HttpClientEngine) {
    install(JsonFeature)
}
```

You have a [full example using JSON](/clients/http-client/examples.html#example-json).

{% include 
    mpp_feature.html
    targets="common,jvm,native,js"
    base="ktor-client-json"
    classifiers=",-jvm,-native,-js"
%}

To use this feature with Kotlin/JS, you need to include the `io.ktor:ktor-client-json-js` artifact.
{: .note.artifact }

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

To use this feature, you need to include `io.ktor:ktor-client-serialization-jvm` artifact on the JVM and `io.ktor:ktor-client-serialization-native` on iOS.
{: .note.artifact }

{% include 
    mpp_feature.html
    targets="common,jvm,native,js"
    base="ktor-client-serialization"
    classifiers=",-jvm,-native,-js"
    hideDescription=true
%}