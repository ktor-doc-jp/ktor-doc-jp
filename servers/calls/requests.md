---
title: リクエスト
caption: HTTPリクエストのハンドリング  
category: servers
permalink: /servers/calls/requests.html
keywords: multipart receiving
redirect_from:
  - /servers/requests.html
ktor_version_review: 1.0.0
---

ルーティングを行うときや直接パイプラインをインターセプトするときには、[ApplicationCall](/servers/calls.html)とともにcontextを取得します。
`call`は`request`というプロパティを持っており、これはリクエストに関する情報を保持しています。

また、`call`自体もリクエストに関係するいくつかの有益なプロパティやメソッドを持っています。

**目次:**

* TOC
{:toc}

## イントロダクション
{: #introduction}

[Routing](/servers/features/routing.html)機能を利用するときや、リクエストのインターセプトを行うときには、
ルートハンドラ内部で`call`プロパティにアクセスできます。
`call`はリクエストに関係する情報をもった`request`プロパティを保持します。

```kotlin
routing {
    get("/") {
        val uri = call.request.uri
        call.respondText("Request uri: $uri")
    } 
}

intercept(ApplicationCallPipeline.Call) { 
    if (call.request.uri == "/") {
        call.respondText("Test String")
    }
}
```

## リクエスト情報
{: #info }

`request`の一部として、内部contextにアクセスできます。

```kotlin
val call: ApplicationCall = request.call
val pipeline: ApplicationReceivePipeline = request.pipeline
```

### URL, メソッド, スキーマ, プロトコル, ホスト, パス, HTTPバージョン, remoteホスト, クライアントIP
{: # info-url }

```kotlin
val version: String = request.httpVersion // "HTTP/1.1"
val httpMethod: HttpMethod = request.httpMethod // GET, POST... 
val uri: String = request.uri // Short cut for `origin.uri`
val scheme: String = request.origin.scheme // "http" or "https"
val host: String? = request.host() // The host part without the port 
val port: Int = request.port() // Port of request
val path: String = request.path() // The uri without the query string
val document: String = request.document() // The last component after '/' of the uri
val remoteHost: String = request.origin.remoteHost // The IP address of the client doing the request
```

### リバースプロキシのサポート: `origin`と`local`
{: #info-origin-local }

（例えばNginxやロードバランサなどがあることで）リバースプロキシの背後にあるとき、受け取ったリクエストはエンドユーザからでなくリバースプロキシからのものになります。
つまり、接続のクライアントIPアドレスがクライアントの代わりにプロキシのものになるということです。
また、リバースプロキシはHTTPSでレスポンスを返す一方、あなたのサーバへはHTTP経由でリクエストします。
多くのリバースプロキシは`X-Forwarded-`ヘッダーをこの情報にアクセスするために送信します。

リバースプロキシ配下で配下で動作するためには、[`XForwardedHeaderSupport` feature](/servers/features/forward-headers.html)をインストールする必要がある点にご注意ください。
{: .note}

リクエストオブジェクトの一部として、`local`と`origin`という2つのプロパティがあり、それを使うことでオリジナルのリクエストやlocal/プロキシのリクエストの情報を取得できます。

```kotlin
val local : RequestConnectionPoint = request.local // Local information 
val origin: RequestConnectionPoint = request.origin // Local / Origin if XForwardedHeaderSupport feature is installed.
```

以下があなたが取得可能なlocal/origin情報です。

```kotlin
interface RequestConnectionPoint {
    val scheme: String // "http" or "https": The provided protocol (local) or `X-Forwarded-Proto`
    val version: String // "HTTP/1.1"
    val port: Int
    val host: String // The provided host (local) or `X-Forwarded-Host`
    val uri: String
    val method: HttpMethod
    val remoteHost: String // The client IP (the direct ip for `local`, or the redirected one `X-Forwarded-For`)
}
```


## GET / クエリパラメータ
{: #get }

クエリパラメータ`?param1=value&param2=value`をコレクションとしてアクセスする必要が出たときには、`queryParameters`が利用できます。
`StringValues`インターフェースを実装しており、各キーがそれに紐づくStringのリストを保持する構造になっています。

```kotlin
val queryParameters: Parameters = request.queryParameters
val param1: String? = request.queryParameters["param1"] // To access a single parameter (first one if repeated)
val repeatedParam: List<String>? = request.queryParameters.getAll("repeatedParam") // Multiple values
```

加工されていない生の`queryString`(`param1=value&param2=value`)にもアクセス可能です。

```kotlin
val queryString: String = request.queryString()
```

## POST, PUT, PATCH

`POST`, `PUT`, `PATCH` リクエストはリクエストボディ（payload）を持ちます。
payloadは通常エンコードされています。

これらのメソッドはクライアントから送信されたpayload全体を読み込み、リクエストボディを2度読み込むと`RequestAlreadyConsumedException`エラーが発生します。
（[DoubleReceive](/servers/features/double-receive.html)機能がインストールされていない限り）
{: .note #receiving-several-times}

### Raw payload
{: #payload-data }

payloadの生のビット列にアクセスするためには、`receiveChannel`が使えます。
`call.request`ではなく`call`の一部として直接アクセスすることになります。

```kotlin
val channel: ByteReadChannel = call.receiveChannel()
```

他にも汎用的な型のための便利なメソッドが提供されています。

```kotlin
val channel: ByteReadChannel = call.receiveChannel()
val text: String = call.receiveText()
val inputStream: InputStream = call.receiveStream() // NOTE: InputStream is synchronous and blocks the thread
val multipart: MultiPartData = call.receiveMultipart()
```

これらreceiveメソッド群は`call.receive<T>`メソッドに対する特定の型の場合のエイリアスです。
`ByteReadChannel`, `ByteArray`, `InputStream`, `MultiPartData`, `String`,`Parameters`の型はデフォルトでインストールされている`ApplicationReceivePipeline.installDefaultTransformations`によってハンドルされます。

### Formパラメータ (urlencodedまたはmultipart)
{: #post }

Form URLencodeまたはmultipartをパースするためには`receiveParameters`か`receive<Parameters>`が使えます。

```kotlin
val postParameters: Parameters = call.receiveParameters()
```

### 型オブジェクト、Content-Type、JSONの受け取り
{: #typed-objects }

`call`はジェネリクスの受け取りもサポートしています。

```kotlin
val obj: T = call.receive<T>()
val obj: T? = call.receiveOrNull<T>()
```

payloadからカスタムのオブジェクトを受け取るためには、
`ContentNegotiation`機能を使う必要があります。
以下はREST APIにおけるJSON payloadの受信、送信の良い例です。

```kotlin
install(ContentNegotiation) {
    gson {
        setDateFormat(DateFormat.LONG)
        setPrettyPrinting()
    }
}
```

ContentNegotiationをgsonを使うように設定した場合、`ktor-gson`アーティファクトをインクルードする必要があります。

```kotlin
compile("io.ktor:ktor-gson:$ktor_version")
```

そして以下の例のように受信ができます。

```kotlin
data class HelloWorld(val hello: String)

routing {
    post("/route") {
        val helloWorld = call.receive<HelloWorld>()
    }
}
```

Gsonに認識されるためクラスはトップレベル（任意のclassや関数の外側で）で定義されている必要があることを忘れてはいけません。
{: .note #receiving-gson-top-level}

### Multipart, ファイル, アップロード
{: #post-files }

[アップロード](/servers/uploads.html)セクションをご覧ください。

### カスタムreceiveトランスフォーマー
{: #custom-receive-transformers }

`application.receivePipeline.intercept(ApplicationReceivePipeline.Transform) { query ->`を呼ぶことで、
カスタムのトランスフォーマーを作成することができ、
`proceedWith(ApplicationReceiveRequest(query.type, transformed))`を呼び出すことで
[ContentNegotiation機能](/servers/features/content-negotiation.html)が実行されます

## Cookie
{: #cookies }

クライアントから送信された`Cookie`ヘッダーにアクセスするための`cookies`プロパティというものがあり、コレクションのように動作します。

```kotlin
val cookies: RequestCookies = request.cookies
val mycookie: String? = request.cookies["mycookie"]
```

cookieを使うことでセッションのハンドルをする場合は、[セッション](/servers/features/sessions.html)機能をご覧ください。

## ヘッダー
{: #headers }

ヘッダーにアクセスするため、リクエストオブジェクトは`headers: Headers`プロパティを持っています。
`StringValues`インターフェースを実装しており、各キーは対応する値としてStringのリストを保持しています。

```kotlin
val headers: Headers = request.headers
val header: String? = request.header("HeaderName") // To access a single header (first one if repeated)
val repeatedHeader: List<String>? = request.headers.getAll("HeaderName") // Multiple values
```

共通的なヘッダーにアクセスするためにいくつかの便利なメソッドがあります。

```kotlin
val contentType: ContentType = request.contentType() // Parsed Content-Tpe 
val contentCharset: Charset? = request.contentCharset() // Content-Type JVM charset
val authorization: String? = request.authorization() // Authorization header
val location: String? = request.location() // Location header
val accept: String? = request.accept() // Accept header
val acceptItems: List<HeaderValue> = request.acceptItems() // Parsed items of Accept header
val acceptEncoding: String? = request.acceptEncoding() // Accept-Encoding header
val acceptEncodingItems: List<HeaderValue> = request.acceptEncodingItems() // Parsed Accept-Encoding items 
val acceptLanguage: String? = request.acceptLanguage() // Accept-Language header
val acceptLanguageItems: List<HeaderValue> = request.acceptLanguageItems() // Parsed Accept-Language items
val acceptCharset: String? = request.acceptCharset() // Accept-Charset header
val acceptCharsetItems: List<HeaderValue> = request.acceptCharsetItems() // Parsed Accept-Charset items
val userAgent: String? = request.userAgent() // User-Agent header
val cacheControl: String? = request.cacheControl() // Cache-Control header
val ranges: RangesSpecifier? = request.ranges() // Parsed Ranges header

val isChunked: Boolean = request.isChunked() // Transfer-Encoding: chunked
val isMultipart: Boolean = request.isMultipart() // Content-Type matches Multipart
```
