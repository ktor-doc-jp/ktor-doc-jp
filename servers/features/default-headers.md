---
title: Default Headers
caption: ヘッダーを自動で送信
category: servers
permalink: /servers/features/default-headers.html
feature:
  artifact: io.ktor:ktor-server-core:$ktor_version
  class: io.ktor.features.DefaultHeaders
redirect_from:
- /features/default-headers.html
ktor_version_review: 1.0.0
---

このFeatureはデフォルトのヘッダーのセットをHTTPレスポンスに付与する機能です。
ヘッダーの一覧はカスタマイズ可能です。

{% include feature.html %}

## 使い方

```kotlin
fun Application.main() {
  ...
  install(DefaultHeaders)
  ...
}
```

`Date`、`Server`ヘッダーを各HTTPレスポンスに付与します。

## 設定
 
* `header(name, value)`は別のヘッダーをデフォルトのヘッダー一覧に追加します。

```kotlin
fun Application.main() {
  ...
  install(DefaultHeaders) {
    header("X-Developer", "John Doe") // will send this header with each response
  }
  ...
}
```

* デフォルトの`Server`ヘッダーは、カスタムのヘッダーを指定することで上書きされます:

```kotlin
fun Application.main() {
  ...
  install(DefaultHeaders) {
    header(HttpHeaders.Server, "Konstructor") 
  }
  ...
}
```

* `Date`ヘッダーは上書きできません。上書きする必要がある場合は、`DefaultHeaders`Featureをインストールせず、代わりにcallを手動でインターセプトするようにしてください。
