---
title: HSTS
caption: HTTP Strict Transport Securityの有効化
keywords: hsts https ssl secure
category: servers
permalink: /servers/features/hsts.html
feature:
  artifact: io.ktor:ktor-server-core:$ktor_version
  class: io.ktor.features.HSTS
redirect_from:
- /features/hsts.html
ktor_version_review: 1.0.0
---

この機能は[RFC 6797](https://tools.ietf.org/html/rfc6797)に従い、 _HTTP Strict Transport Security_ ヘッダーをリクエストに追加する機能です。

HSTSポリシーヘッダーはセキュアでないHTTP接続上では無視されます。HSTSを機能させるためには、セキュアな接続（https）上で配信してください。
{: .note} 

ブラウザはHSTSポリシーヘッダーを受け取ると、指定された期間の間セキュアでない接続ではサーバに接続しようとしません。

{% include feature.html %}

## 使い方

```kotlin
fun Application.main() {
  // ...
  install(HSTS) 
  // ...
}
```

上のコードは、HSTSをデフォルト設定でインストールするコードです。

## 設定

* `maxAge` (デフォルト値は1年): HSTSホストとしてクライアントに記憶させておく期間
* `includeSubDomains` (デフォルト値はtrue): ポリシーを適用したいドメインおよびサブドメインを指定
* `preload` (デフォルト値はfalse): Webブラウザの[preloading list](https://https.cio.gov/hsts/#hsts-preloading)にドメインを含めることを許可する
* `customDirectives` (デフォルト値はempty): 特定のユーザエージェントによってサポートされる任意のカスタムディレクティブ