---
title: オートHeadレスポンス
caption: オートHEADレスポンス機能の有効化
category: servers
permalink: /servers/features/autoheadresponse.html
feature:
  artifact: io.ktor:ktor-server-core:$ktor_version
  class: io.ktor.features.AutoHeadResponse
redirect_from:
- /features/autoheadresponse.html
ktor_version_review: 1.0.0
---

Ktorは自動的に、GETで定義されたルーティングに対する`HEAD`リクエストに対してレスポンスを返すことができます。

{% include feature.html %}

## 使い方

自動で`HEAD`レスポンスを返す設定を有効化するには、`AutoHeadResponse` Featureをインストールしてください。

```kotlin
fun Application.main() {
  // ...
  install(AutoHeadResponse) 
  // ...
}
```

## 設定オプション

ありません。

## 内部的な隠蔽されている挙動

この機能は自動的に`HEAD`リクエストに対し、まるで`GET`レスポンスにルーティングされたうえでbody部分を破棄したかのようにレスポンスを返します。
システムによって生成される`FinalContent`は遅延評価されるので、ボディー付きの`GET`リクエストを処理する上でパフォーマンスのコストがかかりません。
