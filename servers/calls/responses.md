---
title: レスポンス
caption: HTTPレスポンスの生成
category: servers
permalink: /servers/calls/responses.html
keywords: Redirections Location header permanent redirect temporal redirect pushing http2 respondFile respondBytes respondText respond response downloads generating response sending response
redirect_from:
  - /servers/responses.html
ktor_version_review: 1.0.0
---

ルーティングのハンドリングまたは直接パイプラインをインターセプトする際に、
[ApplicationCall](/servers/calls.html)からcontextを取得できます。
`call`は`response`と呼ばれるプロパティを持っており、それを使うことでレスポンスの送信を行うことができます。

また、`call`自体もレスポンスを操作するような便利なプロパティやメソッドをいくつか持っています。

**目次:**

* TOC
{:toc}

## Context
{: #context}

[Routing](/servers/features/routing.html)機能を利用する際には、
ルートハンドラ内で`call`プロパティにアクセスすることができるようになります。

```kotlin
routing {
    get("/") {
        call.respondText("Request uri: ${call.request.uri}")
    } 
}
```

リクエストのインターセプトを行う際に、`intercept`内のlambda関数が`call`プロパティを持っており、それを利用することもできます。

```kotlin
intercept(ApplicationCallPipeline.Call) { 
    if (call.request.uri == "/") {
        call.respondText("Test String")
    }
}
```

## HTTPヘッダーとステータスの操作
{: #properties}

HTTPステータス、ヘッダー、Cookie、payloadなど、レスポンスがどのように生成されるのかを操作することができます。

HTTPリクエストとレスポンスはシークできないストリームであるため、
一度レスポンスpayload/コンテンツを送信し始めると、
ステータスやヘッダーが送信され、
ステータスやヘッダーやCookieを変更することができなくなる点にご注意ください。
{: .note #headers-already-sent } 

`response`の一部として、以下のような内部contextにもアクセスできます。

* `val call: ApplicationCall = response.call`
* `val pipeline: ApplicationSendPipeline = response.pipeline`

ヘッダー:

* `val headers: ResponseHeaders = response.headers`

`Set-Cookie`ヘッダーをセットするための便利な`cookies`インスタンス:

* `val cookies: ResponseCookies = response.cookies`

HTTP Statusの取得と変更:

* `response.status(HttpStatusCode.OK)` - 事前に定義されたHTTPステータスコードの設定
* `response.status(HttpStatusCode(418, "I'm a tea pot"))` - カスタムのHTTPステータスコードの設定
* `val status: HttpStatusCode? = response.status()` - 現在設定されているHTTPステータスコードの取得

* `response.contentType(ContentType.Text.Plain.withCharset(Charsets.UTF_8))` - 定義されている型を使いContent-Typeを設定（`ContentType.Application.Json`においてはUTF-8がデフォルトの文字コードです）
* `response.contentType("application/json; charset=UTF-8")` - 定義されている型を使わずContent-Typeを設定

カスタムヘッダー:

* `response.header("X-My-Header", "my value")` - カスタムヘッダーの追加
* `response.header("X-My-Times", 1000)` - カスタムヘッダーの追加
* `response.header("X-My-Times", 1000L)` - カスタムヘッダーの追加
* `response.header("X-My-Date", Instant.EPOCH)` - カスタムヘッダーの追加

通常インフラストラクチャによって設定されるヘッダーをセットする便利なメソッド群:

* `response.etag("33a64df551425fcc55e4d42a148795d9f25f89d4")` - キャッシュのため`ETag`の設定
* `response.lastModified(ZonedDateTime.now())` - `Last-Modified`ヘッダーの設定
* `response.contentLength(1024L)` - `Content-Length`の設定。一般的にはpayload送信時に自動で付与されるものです。
* `response.cacheControl(CacheControl.NoCache(CacheControl.Visibility.Private))` - 定義されている型を使いCache-Controlの設定
* `response.expires(LocalDateTime.now())` - `Expires`ヘッダーの設定
* `response.contentRange(1024L until 2048L, 4096L)` - `Content-Range`ヘッダーの設定([PartialContent](/servers/features/partial-content.html)機能をご覧ください) 

## HTTP/2 pushingとHTTP/1 `Link` ヘッダー
{: #pushing}

`call`はpushingをサポートしています。

* HTTP/2において、push機能が使えます。
* HTTP/1.2において、`Link`ヘッダーをヒントとして付与できます。

```kotlin
routing {
    get("/") {
        call.push("/style.css")
    }
}
```

Pushingはリクエストからページの表示までの間の時間を削減します。
しかし、コンテンツを事前に送信することはクライアントにすでにキャッシュされているコンテンツを送信することになるかもしれないので用心してください。
{: .note.performance }

## リダイレクト

`respondRedirect`メソッドを使うことでリダイレクトレスポンスを簡単に生成することができます。
`301 Moved Permanently`か`302 Found`によるリダイレクトを`Location`ヘッダーを使って行います。

```kotlin
call.respondRedirect("/moved/here", permanent = true)
```

この関数が実行されると、残りの関数も実行されることに注意してください。
したがって、ガード句内で利用する場合は、ハンドラーの残りの部分を継続して処理しないよう関数からreturnする必要があります。
例外を投げることによって制御フローを止めるリダイレクトを行いたい場合は、[ステータスページのサンプル](/servers/features/status-pages.html#redirect)をご覧ください。
{: .note}

## レスポンスコンテンツの送信

ジェネリクスのコンテンツを送信（[コンテントネゴシエーション](#content-negotiation)と互換性があります）
{: #call-respond}

* `call.respond(MyDataClass("hello", "world"))` - [コンテンツネゴシエーション](#content-negotiation)セクションをご確認ください
* `call.respond(HttpStatusCode.NotFound, MyDataClass("hello", "world"))` - ステータスコードを指定しpayloadを送信するのを1回の呼び出しで行います。[ステータスページ](/servers/features/status-pages.html)をご確認ください。

プレーンテキストの送信:

* `call.respondText("text")` - 単なる文字列をBodyに入れる
* `call.respondText("p { background: red; }", contentType = ContentType.Text.CSS, status = HttpStatusCode.OK) { ... }` - ContentType、HTTPステータスを指定しテキストを送信し、[OutgoingContent](#outgoing-content)の設定を行う
* `call.respondText { "string" }` - suspendプロバイダで文字列を返す
* `call.respondText(contentType = ..., status = ...) { "string" }` - suspendプロバイダで文字列を返す
* `call.respond(TextContent("{}", ContentType.Application.Json))` - `Content-Type`に文字コードを追加し文字列を返す 

byte arrayの送信:

* `call.respondBytes(byteArrayOf(1, 2, 3))` - binaryのbodyでByteArray

ファイルの送信:

* `call.respondFile(File("/path/to/file"))` - ファイルの送信
* `call.respondFile(File("basedir"), "filename") { ... }` - ファイルを送信し、[OutgoingContent](#outgoing-content)を設定

URL-encoded form(`application/x-www-form-urlencoded`)の送信:

* `Parameters.formUrlEncode`が利用できます.詳細は[Utilitiesページ](/advanced/utilities.html)をご確認ください。

リクエストパラメータに基づいてファイルを送信するとき、入力のバリデーションと制限方法について特に注意してください。
{: .note.security #validate-respond-file-parameters }

Writerを使いchunked contentを送信:

* `call.respondWrite { write("hello"); write("world") }` - writerを使いテキストを送信。[HTML DSL](#html-dsl)と一緒に使えます。
* `call.respondWrite(contentType = ..., status = ...) { write("hello"); write("world") }` - writerを使いテキストを送信し、contentTypeとステータスを指定します。

`WriteChannelContent`を使いチャンク内の任意のデータを送信:

```kotlin
call.respond(object : OutgoingContent.WriteChannelContent() {
    override val contentType = ContentType.Application.OctetStream
    override suspend fun writeTo(channel: ByteWriteChannel) {
        channel.writeFully(byteArray1)
        channel.writeFully(byteArray2)
        // ...
    }
})
```

リクエストのため、デフォルトのcontentTypeを指定:

* `call.defaultTextContentType(contentType: ContentType?): ContentType`

レスポンス設定のためのOutgoingContentインターフェース:
{: #outgoing-content}

```kotlin
class OutgoingContent {
    val contentType: ContentType? get() = null // * Specifies [ContentType] for this resource.
    val contentLength: Long? get() = null // Specifies content length in bytes for this resource. - If null, the resources will be sent as `Transfer-Encoding: chunked` 
    val status: HttpStatusCode? // Status code to set when sending this content
    val headers: Headers // Headers to set when sending this content
    fun <T : Any> getProperty(key: AttributeKey<T>): T? = extensionProperties?.getOrNull(key) // Gets an extension property for this content
    fun <T : Any> setProperty(key: AttributeKey<T>, value: T?) // Sets an extension property for this content
}
```

## ファイルをダウンロード可能にする
{: #content-disposition }

[`Content-Disposition`ヘッダー](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Disposition)を付与することでファイルをダウンロード可能にすることができます。

定義されている型を使わない方法だと、以下のように使えます:

```kotlin
call.response.header(HttpHeaders.ContentDisposition, "attachment; filename=\"myfilename.bin\"")
```

しかしKtorは定義されている方も提供しており、その方法だと適切なエスケープとヘッダーの生成を行ってくれます:

```kotlin
call.response.header(HttpHeaders.ContentDisposition, ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "myfilename.bin").toString())
```

## コンテントネゴシエーション
{: #content-negotiation}

コンテントネゴシエーションのためのプラグインの設定をするとき、
パイプラインは[`call.respond`](#call-respond)メソッドのための追加の型を受け入れるかもしれません。

### HTMLをDSLで送信
{: #html-dsl}

Ktorはオプショナルな機能として[HTMLコンテンツをDSLを使って送信](/servers/features/templates/html-dsl.html)する機能を持ちます.

### HTMLをFreeMarkerで送信
{: #html-freemarker}

Ktorはオプショナルな機能として[HTMLコンテンツをFreeMarkerを使って送信](/servers/features/templates/freemarker.html)する機能を持ちます.

### JSONをdata classを使って送信
{: #json}

Ktorはオプショナルな機能として[JSONコンテンツをContentNegotiationを使って送信](/servers/features/templates/freemarker.html)する機能を持ちます.


