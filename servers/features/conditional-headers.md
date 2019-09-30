---
title: Conditional Header
caption: 簡単な'304 Not Modified'レスポンス
category: servers
permalink: /servers/features/conditional-headers.html
keywords: etag last-modified
feature:
  artifact: io.ktor:ktor-server-core:$ktor_version
  class: io.ktor.features.ConditionalHeaders
redirect_from:
- /features/conditional-headers.html
ktor_version_review: 1.0.0
---

ConditionalHeaders Featureを使うことで、クライアントが同じコンテンツをすでに持っている場合にコンテンツの送信をしないようにすることができます。
`Resource`または`FinalContent`が持つ、`ETag`か`LastModified`プロパティをチェックすることによって実現されます。
条件で許可されている場合、コンテンツ全体は送信されず"304 Not Modified"レスポンスが代わりに送信されます。

{% include feature.html %}

## 設定

`ConditionalHeaders`をインストールすると追加の設定無しにと利用ができます:

```kotlin
install(ConditionalHeaders)
```

パラメータとして渡される生成された`OutgoingContent`からバージョンの一覧を取得するようなlambdaを設定することができます:

```kotlin
install(ConditionalHeaders) {
    version { content -> listOf(EntityTagVersion("tag1")) }
}
```

## 拡張性

`Version`インターフェースの実装は`Resource`インスタンスに紐付き、独自ロジックでのカスタム実装を返すことができます。
`FinalContent`は`ETag`か`LastModified`ヘッダーに対してのみチェックされることに注意してください。

