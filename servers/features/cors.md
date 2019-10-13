---
title: CORS
caption: Cross-Origin Resource Sharing (CORS)の有効化
category: servers
permalink: /servers/features/cors.html
feature:
  artifact: io.ktor:ktor-server-core:$ktor_version
  class: io.ktor.features.CORS
redirect_from:
- /features/cors.html
ktor_version_review: 1.0.0
---

KtorはデフォルトでCross-Origin Resource Sharing (CORS)の適切な実装をサポートするインターセプタを提供しています。

> Cross-Origin Resource Sharing (CORS)はドメイン境界をまたぐアクセスを有効にする仕様です。パブリックなコンテンツを配信する場合、Javascriptやブラウザアクセスから普遍的にアクセス可能にできるようにするためCORSを検討してください。 
*参考: [enable-cors.org](http://enable-cors.org/)*

{% include feature.html %}

## 基本

まず初めに、CORS Featureをアプリケーションにインストールします。

```kotlin
fun Application.main() {
  ...
  install(CORS)
  ...
}
```

CORS Featureのデフォルトの設定は`GET`、`POST`、`HEAD`HTTPメソッドと以下のヘッダーのみ扱えます:

```kotlin
  HttpHeaders.Accept
  HttpHeaders.AcceptLanguages
  HttpHeaders.ContentLanguage
  HttpHeaders.ContentType
```

## 発展的内容

 - [ソースコード](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-core/jvm/src/io/ktor/features/CORS.kt)
 - [テスト](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-tests/test/io/ktor/tests/http/CORSTest.kt)

以下はCORSを有効にしているAPI関数を表す発展的な例です。

```kotlin
fun Application.main() {
  ...
  install(CORS)
  {
    method(HttpMethod.Options)
    header(HttpHeaders.XForwardedProto)
    anyHost()
    host("my-host")
    // host("my-host:80")
    // host("my-host", subDomains = listOf("www"))
    // host("my-host", schemes = listOf("http", "https"))
    allowCredentials = true
    allowNonSimpleContentTypes = true
    maxAge = Duration.ofDays(1)
  }
  ...
}
```

## 設定

- `method("HTTP_METHOD")` : 設定したHTTPメソッドを、CORSを利用するHTTPメソッド群ホワイトリストに追加します。
- `header("header-name")` : 設定したHeaderをCORSを利用するヘッダー群ホワイトリストに追加します。
- `exposeHeader("header-name")` : レスポンス内にこのヘッダーを追加します。
- `exposeXHttpMethodOverride()` : レスポンス内に`X-Http-Method-Override`ヘッダーを追加します。
- `anyHost()` : リソースに任意のホストからアクセスすることを許可します。
- `host("hostname")` : 指定したホストのみCORSを使えるようにします。port番号やサブドメイン一覧やschemaを指定することもできます。
- `allowCredentials` : `Access-Control-Allow-Credentials`ヘッダーをレスポンスに含めます。
- `allowNonSimpleContentTypes`: `Content-Type`リクエストヘッダーを、[simple content type](https://www.w3.org/TR/cors/#simple-header)以外のホワイトリスト値として追加することができます。   
- `maxAge`: 指定したmaxAgedで`Access-Control-Max-Age`ヘッダーをレスポンスに含めます。

