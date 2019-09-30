---
title: Gson
caption: Gsonを利用したJSONのサポート
category: servers
feature:
  artifact: io.ktor:ktor-gson:$ktor_version
  class: io.ktor.gson.GsonConverter
redirect_from:
- /features/gson.html
- /features/content-negotiation/gson.html
ktor_version_review: 1.0.0
---

gson機能を使うと、JSONコンテンツをあなたのアプリケーション内で[google-gson](https://github.com/google/gson)を使って簡単に扱うことができるようになります。

この機能は[ContentNegotiation](/servers/features/content-negotiation.html)コンバーターです。

{% include feature.html %}

## 基本的な使い方

Gsonを使ったJSONコンテンツコンバーターを登録することでこの機能をインストールします:

```kotlin
install(ContentNegotiation) {
    gson {
        // Configure Gson here
    }
}
```

`gson`ブロックは以下の意味を持つ便利なメソッドです:

```kotlin
register(ContentType.Application.Json, GsonConverter(GsonBuilder().apply {
    // ...
}.create()))
```

## 設定

`gson`ブロック内で、ContentNegotiationをインストールするために使われた[GsonBuilder](https://google.github.io/gson/apidocs/com/google/gson/GsonBuilder.html)にアクセスできます。
以下を、何ができるのかの参考にしてください:

```kotlin
install(ContentNegotiation) {
    gson {
        setPrettyPrinting()
        
        disableHtmlEscaping()
        disableInnerClassSerialization()
        enableComplexMapKeySerialization()

        serializeNulls()

        serializeSpecialFloatingPointValues()
        excludeFieldsWithoutExposeAnnotation()
        
        setDateFormat(...)

        generateNonExecutableJson()

        setFieldNamingPolicy()
        setLenient()
        setLongSerializationPolicy(...)
        setExclusionStrategies(...)
        setVersion(0.0)
        addDeserializationExclusionStrategy(...)
        addSerializationExclusionStrategy(...)
        excludeFieldsWithModifiers(Modifier.TRANSIENT)
        
        registerTypeAdapter(...)
        registerTypeAdapterFactory(...)
        registerTypeHierarchyAdapter(..., ...)
    }
}
```