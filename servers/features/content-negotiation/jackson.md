---
title: Jackson
caption: Jacksonを利用したJSONサポート
category: servers
feature:
  artifact: io.ktor:ktor-jackson:$ktor_version
  class: io.ktor.jackson.JacksonConverter
redirect_from:
- /features/jackson.html
- /features/content-negotiation/jackson.html
ktor_version_review: 1.0.0
---

Jackson機能を使うと、JSONコンテンツをあなたのアプリケーション内で[jackson](https://github.com/FasterXML/jackson)ライブラリを使って簡単に扱うことができるようになります。

この機能は[ContentNegotiation](/servers/features/content-negotiation.html)コンバーターです。

{% include feature.html %}

## 基本的な使い方

Jacksonを使ったJSONコンテンツコンバーターを登録することでこの機能をインストールします:

```kotlin
install(ContentNegotiation) {
    jackson {
        // Configure Jackson's ObjectMapper here
    }
}
```

`jackson`ブロックは以下の意味を持つ便利なメソッドです:

```kotlin
register(ContentType.Application.Json, JacksonConverter(ObjectMapper().apply {
    // ...
}.create()))
```

## 設定

`jackson`ブロック内で、ContentNegotiationをインストールするために使われた[ObjectMapper](https://fasterxml.github.io/jackson-databind/javadoc/2.9/com/fasterxml/jackson/databind/ObjectMapper.html)にアクセスできます。
以下を、何ができるのかの参考にしてください:

```kotlin
install(ContentNegotiation) {
    jackson {
        enable(SerializationFeature.INDENT_OUTPUT)
        enable(...)
        dateFormat = DateFormat.getDateInstance()
        disableDefaultTyping()
        convertValue(..., ...)
        ...
    }
}
```