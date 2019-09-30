---
title: kotlinx.serialization
caption: kotlinx.serializationを利用したJSONサポート
category: servers
feature:
  artifact: io.ktor:ktor-serialization:$ktor_version
  class: io.ktor.serialization.SerializationConverter
redirect_from:
- /features/serialization.html
- /features/content-negotiation/serialization.html
ktor_version_review: 1.2.3
---

SerializationConverter機能を使うと、JSONコンテンツをあなたのアプリケーション内で[kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)ライブラリを使って簡単に扱うことができるようになります。

この機能は[ContentNegotiation](/servers/features/content-negotiation.html)コンバーターです。

{% include feature.html %}

## 基本的な使い方

kotlinx.serializationを使ったJSONコンテンツコンバーターを登録することでこの機能をインストールします:

```kotlin
install(ContentNegotiation) {
    serialization()
}
```

## 設定

`serialization()`関数は2つのオプショナルのパラメータをデフォルト引数付きで持っています。
* `contentType`は、どのcontent typeがハンドリングされるかを指定できます。`ContentType.Application.Json`がデフォルト値です。
* `json`は、[JSONフォーマッター](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/runtime_usage.md#json)を設定する機能を提供します。`Json(DefaultJsonConfiguration)`がデフォルト値です。

発展的な例:
```kotlin
install(ContentNegotiation) {
    serialization(
        contentType = ContentType.Application.Json,
        json = Json(
            DefaultJsonConfiguration.copy(
                prettyPrint = true
            )
        )
    )
}
```

