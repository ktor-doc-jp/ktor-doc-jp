---
title: レスポンス
caption: HTTPクライアントレスポンス
category: clients
permalink: /clients/http-client/call/responses.html
redirect_from:
- /clients/http-client/calls/responses.html
ktor_version_review: 1.2.0
---

## レスポンスボディの受信

{: #receive}

デフォルトでは、Httpクライアントリクエストの可能な型として`HttpResponse`または`String`を使用できます。
例えば、次のとおりです。

```kotlin
val htmlContent = client.get<String>("https://en.wikipedia.org/wiki/Main_Page")
val response = client.get<HttpResponse>("https://en.wikipedia.org/wiki/Main_Page")
```

*JsonFeature* が設定されていて、サーバーがヘッダー`Content-Type: application/json`を返す場合、
デシリアライズするクラスを指定することもできます。

```kotlin
val helloWorld = client.get<HelloWorld>("http://127.0.0.1:8080/")
```

### `HttpResponse`クラス

{: #HttpResponse }

`HttpResponse`のAPIリファレンスは[こちらに記載されています](https://api.ktor.io/{{site.ktor_version}}/io.ktor.client.response/-http-response/).

`HttpResponse`から、レスポンス内容を簡単に取得することができます。

* `val readChannel: ByteReadChannel = response.content`
* `val bytes: ByteArray = response.readBytes()`
* `val text: String = response.readText()`
* `val readChannel = response.receive<ByteReadChannel>()`
* `val multiPart = response.receive<MultiPartData>()`
* `val inputStream = response.receive<InputStream>()` *InputStream APIは同期的なことに注意！*
* `response.discardRemaining()`

ステータス、ヘッダー、内部状態などの追加のレスポンス情報も取得できます。

### *Basic*

* `val status: HttpStatusCode = response.status`
* `val headers: Headers = response.headers`

### *Advanced*

* `val call: HttpClientCall = response.call`
* `val version: HttpProtocolVersion = response.version`
* `val requestTime: Date = response.requestTime`
* `val responseTime: Date = response.responseTime`
* `val executionContext: Job = response.executionContext`

### *Extensions for headers*

* `val contentType: ContentType? = response.contentType()`
* `val charset: Charset? = response.charset()`
* `val lastModified: Date? = response.lastModified()`
* `val etag: String? = response.etag()`
* `val expires: Date? = response.expires()`
* `val vary: List<String>? = response.vary()`
* `val contentLength: Int? = response.contentLength()`
* `val setCookie: List<Cookie> = response.setCookie()`
