---
title: リクエスト
caption: リクエストの準備
category: clients
permalink: /clients/http-client/quick-start/requests.html
redirect_from:
- /clients/http-client/call/requests.html
ktor_version_review: 1.3.0
---

## リクエストの作成

クライアントの設定が完了したので、次はリクエストの実行の準備をしましょう。
ほとんどの単純なリクエストは、下記のパターンになります。

```kotlin
val response = client.'http-method'<'ResponseType'>("url-string")
```

Kotlin のジェネリクス型の型推論を用いると、さらにシンプルになります。

```
val response: ResponseType = client.'http-method'("url-string")
```

例えば レスポンスすべてを `String` 型で受け取るために `GET` リクエストするには下記のように記述します。

```kotlin
val htmlContent = client.get<String>("https://en.wikipedia.org/wiki/Main_Page")
// 下記と同じ
val content: String = client.get("https://en.wikipedia.org/wiki/Main_Page")
```

生のビット列に興味がある場合は、 `ByteArray` 型で受け取ります。

```kotlin
val channel: ByteArray = client.get("https://en.wikipedia.org/wiki/Main_Page")
```

完全な [HttpResponse](https://api.ktor.io/{{ site.ktor_version }}/io.ktor.client.statement/-http-response/index.html) が必要な場合は `HttpResponse` 型で受け取ります。

```kotlin
val response: HttpResponse = client.get("https://en.wikipedia.org/wiki/Main_Page")
```

デフォルトでは、 [HttpResponse](https://api.ktor.io/{{ site.ktor_version }}/io.ktor.client.statement/-http-response/index.html) はすべてメモリ上にダウンロードされます。
レスポンスを部分的にダウンロードする方法やストリームデータを扱う方法については、 [ストリーミング](/clients/http-client/quick-start/streaming.html) 章を参照してください。

そして、 [Json](/clients/http-client/features/json-feature.html) feature を用いることで、自作のデータクラスに格納できます。

```kotlin
@Serializable
data class User(val id: Int)

val response: User = client.get("https://myapi.com/user?id=1")
```

いくつかのレスポンス型は `Closeable` を実装している場合があり、その場合はリソースを保持し続けることに注意してください。

## リクエストのカスタマイズ

我々は *GET* リクエストだけでは生きていけません。
Ktor には、どの HTTP メソッドも利用可能で、複雑なリクエストを作成でき、様々な方法でレスポンスを処理することが可能な柔軟性があります。

### デフォルトの HTTP メソッド

{: #shortcut-methods }

`request` と同様に、一般的な HTTP メソッド (`GET`, `POST`, `PUT`, `DELETE`, `PATCH`, `HEAD`, `OPTIONS`) でリクエストを実行するための拡張関数があります。


```kotlin
val text = client.post<String>("http://127.0.0.1:8080/")
```

リクエストメソッドを呼ぶ際に、ラムダ式で URL 、 HTTP メソッド、リクエストボディ、ヘッダなどのリクエストパラメータを指定することができます。

```kotlin
val text = client.post<String>("http://127.0.0.1:8080/") {
    header("Hello", "World")
}
```

[HttpRequestBuilder](https://api.ktor.io/{{ site.ktor_version }}/io.ktor.client.request/-http-request-builder/) は下記のようになっています。

```kotlin
class HttpRequestBuilder : HttpMessageBuilder {
    var method: HttpMethod

    val url: URLBuilder
    fun url(block: URLBuilder.(URLBuilder) -> Unit)

    val headers: HeadersBuilder
    fun header(key: String, value: String)
    fun headers(block: HeadersBuilder.() -> Unit)

    var body: Any = EmptyContent

    val executionContext: CompletableDeferred<Unit>
    fun setAttributes(block: Attributes.() -> Unit)
    fun takeFrom(builder: HttpRequestBuilder): HttpRequestBuilder
}
```

`HttpClient` クラスはいくつかの基本的な機能のみ提供しており、リクエストを構築するためのメソッドは拡張関数として提供されます。

[`HttpClient` ビルダー拡張関数](https://api.ktor.io/{{ site.ktor_version }}/io.ktor.client.request/) に記載があります。

### メソッドのカスタマイズ

呼び出しに加えて、型付きのリクエストとして振る舞う `request` メソッドがあり、 `String` 、 `HttpResponse` 、または任意の型を指定可能です。
リクエストを作成する際に、 URL と HTTP メソッドを指定する必要があります。

```kotlin
val call = client.request<String> {
    url("http://127.0.0.1:8080/")
    method = HttpMethod.Get
}
```

### form の POST (送信)

{: #submit-form }

form の情報を送信するのに便利な拡張関数がいくつか用意されています。
詳細は [こちら](https://api.ktor.io/{{ site.ktor_version }}/io.ktor.client.request.forms/) に記載があります。

`submitForm` メソッド

```kotlin
client.submitForm(
    formParameters: Parameters = Parameters.Empty,
    encodeInQuery: Boolean = false,
    block: HttpRequestBuilder.() -> Unit = {}
)
```

クエリ文字列にエンコードされた `Parameters` でリクエストする (`GET` ではデフォルト) か、 `encodeInQuery` パラメータに応じて multipart としてエンコードされた `Parameters` でリクエストする (`POST` ではデフォルト) ことができます。

`submitFormWithBinaryData` メソッド

```kotlin
client.submitFormWithBinaryData(
    formData: List<PartData>,
    block: HttpRequestBuilder.() -> Unit = {}
): T
```

`PartData` のリストから、 multipart の POST リクエストをを生成できます。
`PartData` は `PartData.FormItem` か、 `PartData.BinaryItem` または `PartData.FileItem` です。

`PartData` のリクエストを生成するために、 `formData` ビルダを利用することができます。

```kotlin
val data: List<PartData> = formData {
    // append 可能な型 : String, Number, ByteArray, Input
    append("hello", "world")
    append("number", 10)
    append("ba", byteArrayOf(1, 2, 3, 4))
    appendInput("input", size = knownSize.orNull()) { openInputStream().asInput() }
    // ヘッダも指定可能
    append("hello", "world", headersOf("X-My-Header" to "MyValue"))
}
```

### カスタムヘッダの指定

{: #custom-headers}

`HttpRequestBuilder` でリクエストを構築する際に、カスタムヘッダを指定することができます。
`StringValuesBuilder` を継承した `val headers: HeadersBuilder` を用います。
You can add or remove headers using it, or with the `header` convenience methods.
`headers` を用いることでヘッダ情報の追加および削除が可能です。
`header` という便利メソッドもあります。

```kotlin
// this : HttpMessageBuilder

// ヘッダを追加する便利メソッド
header("My-Custom-Header", "HeaderValue")

// headers: HeadersBuilder のメソッド呼び出しでヘッダを構築する例
headers.clear()
headers.append("My-Custom-Header", "HeaderValue")
headers.appendAll("My-Custom-Header", listOf("HeaderValue1", "HeaderValue2"))
headers.remove("My-Custom-Header")

// `headers` 便利ビルダー関数を用いたヘッダ情報の構築
headers { // this: HeadersBuilder
    clear()
    append("My-Custom-Header", "HeaderValue")
    appendAll("My-Custom-Header", listOf("HeaderValue1", "HeaderValue2"))
    remove("My-Custom-Header")
}
```

`HeaderBuilder` API の全容は [こちら](https://api.ktor.io/{{ site.ktor_version }}/io.ktor.http/-headers-builder/) にあります。

## Specifying a body for requests

`POST` や `PUT` リクエストでは、 `body` プロパティの指定か可能です。

```kotlin
client.post<Unit> {
    url("http://127.0.0.1:8080/")
    body = // ...
}
```

`HttpRequestBuilder.body` プロパティには `OutgoingContent` 型 (およびそのサブタイプ型) または `String` 型のインスタンスを指定することができます。

* `body = "HELLO WORLD!"`
* `body = TextContent("HELLO WORLD!", ContentType.Text.Plain)`
* `body = ByteArrayContent("HELLO WORLD!".toByteArray(Charsets.UTF_8))`
* `body = LocalFileContent(File("build.gradle"))`
* `body = JarFileContent(File("myjar.jar"), "test.txt", ContentType.fromFileExtension("txt").first())`
* `body = URIFileContent("https://en.wikipedia.org/wiki/Main_Page")`

**JsonFeature** をインストール済で content type に `application/json` を指定している場合、
`body` に任意の型のインスタンスを指定することで、自動的に JSON にシリアライズされます。

```kotlin
data class HelloWorld(val hello: String)

val client = HttpClient(Apache) {
    install(JsonFeature) {
        serializer = GsonSerializer {
            // GsonBuilder の設定
            serializeNulls()
            disableHtmlEscaping()
        }
    }
}

client.post<Unit> {
    url("http://127.0.0.1:8080/")
    body = HelloWorld(hello = "world")
}
```

別法 (組み込みの `JsonSerializer` を利用)

```kotlin
val json = io.ktor.client.features.json.defaultSerializer()
client.post<Unit>() {
    url("http://127.0.0.1:8080/")
    body = json.write(HelloWorld(hello = "world")) // OutgoingContent が生成される
}
```

別法 (Jackson を利用 (JVM のみ))


```kotlin
val json = jacksonObjectMapper()
client.post<Unit> {
    url("http://127.0.0.1:8080/")
    body = TextContent(json.writeValueAsString(userData), contentType = ContentType.Application.Json)
}
```

**トップレベルではない** クラスを `Gson` が認識しないことに注意してください。 \\
関数内にあるクラスを送信しようとすると、 **`null`** が送信されます。
{: .note}

## multipart/form-data のアップロード

{: #multipart-form-data }

Ktor HTTP クライアントは MultiPart リクエストの生成をサポートしています。
リクエストの body にて `MultiPartFormDataContent(parts: List<PartData>)` を `OutgoingContent` として利用することで実現できます。

最も簡単な使用方法は [`submitFormWithBinaryData` メソッド](#submit-form) を参照してください。

あるいは、 body に直接指定することもできます。

```kotlin
val request = client.request {
    method = HttpMethod.Post
    body = MultiPartFormDataContent(formData {
        append("key", "value")
    })
}
```