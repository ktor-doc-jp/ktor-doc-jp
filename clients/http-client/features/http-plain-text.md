---
title: Text & Charsets
category: clients
caption: Text & Charsets
feature:
  artifact: io.ktor:ktor-client-core:$ktor_version
  class: io.ktor.client.features.HttpPlainText
ktor_version_review: 1.2.0
---

この機能により、要求と応答のプレーンテキストコンテンツを処理できます。
登録済みの文字セットで`Accept`ヘッダーを埋め、`ContentType`文字セットに従ってリクエスト本文をエンコードし、レスポンス本文をデコードします。

{% include feature.html %}

## 設定

HTTP callプロパティに何も設定されていなかった場合、`Charsets.UTF_8`がデフォルト値として利用されます。

```kotlin
val client = HttpClient(HttpClientEngine) {
    Charsets {
        // Allow to use `UTF_8`.
        register(Charsets.UTF_8)

        // Allow to use `ISO_8859_1` with quality 0.1.
        register(Charsets.ISO_8859_1, quality=0.1f)

        // Specify Charset to send request(if no charset in request headers).
        sendCharset = ...

        // Specify Charset to receive response(if no charset in response headers).
        responseCharsetFallback = ...
    }
}
```

{: .note}
