---
title: Requests
caption: Handling HTTP Requests  
section: Servers
permalink: /servers/requests.html
---

When handling routes, or directly intercepting the pipeline, you
get a context with an [ApplicationCall](/servers/application.html#applicationcall).
That `call` contains a property called `requests` that includes information about the request.

**Table of contents:**

* TOC
{:toc}

## Context
{: #context}

When using the [Routing](/features/routing.html) feature, you can access
the `call` property inside route handlers:

```kotlin
routing {
    get("/") {
        call.respondText("Request uri: ${call.request.uri}")
    } 
}
```

When intercepting requests, the lambda handler in `intercept` has the `call` property available too:

```kotlin
intercept(ApplicationCallPipeline.Call) { 
    if (call.request.uri == "/") {
        call.respondText("Test String")
    }
}
```

## Properties of the `ApplicationRequest` interface
{: #properties}

As part of the request, you can get access to its internal context:

```kotlin
val call: ApplicationCall = request.call
val pipeline: ApplicationReceivePipeline = request.pipeline
```

You can also access the information about the connection point (either local, or original
if under a proxy, and the proxy sent header information):

```kotlin
val local : RequestConnectionPoint = request.local  // local information 
val origin: RequestConnectionPoint = request.origin // might use proxy headers
val uri: String = request.uri // Short cut for `origin.uri`
val document: String = request.document() // The last component after '/' of the uri
val path: String = request.path() // The uri without the query string
val host: String? = request.host() // The host part without the port 
val port: Int = request.port() // Port of request
```

```kotlin
interface RequestConnectionPoint {
    val scheme: String
    val version: String
    val port: Int
    val host: String
    val uri: String
    val method: HttpMethod
    val remoteHost: String
}
```

You can access the `HttpMethod` and the `httpVersion` used for the request:

```kotlin
val httpMethod: HttpMethod = request.httpMethod
val httpVersion: String = request.httpVersion
```

If you need to access the query parameters `?param1=value&param2=value` as a collection,
you can use `queryParameters`. The same happens with `headers: Headers`. Both types
implement the `StringValues` interface where each key can have a list of Strings associated.

```kotlin
val queryParameters: Parameters = request.queryParameters
val queryString: String = request.queryString()
val headers: Headers = request.headers

val param1: String? = request.queryParameters["param1"]
val repeatedParam: List<String>? = request.queryParameters.getAll("repeatedParam")
// ...
```

There is a `cookies` property to access the `Cookie` headers sent by the client,
just as if it was a collection:

```kotlin
val cookies: RequestCookies = request.cookies
val mycookie: String? = request.cookies["mycookie"]
```

And several convenience methods to access headers:

```kotlin
val header: String? = request.header("HeaderName")
val contentType: ContentType = request.contentType() // Parsed Content-Tpe 
val contentCharset: Charset? = request.contentCharset() // Content-Type JVM charset
val authorization: String? = request.authorization() // Authorization header
val location: String? = request.location() // Location header
val accept: String? = request.accept() // Accept header
val acceptItems: List<HeaderValue> = request.acceptItems() // Parsed items of Accept header
val acceptEncoding: String? = request.acceptEncoding() // Accept-Encoding header
val acceptEncodingItems: List<HeaderValue> = request.acceptEncodingItems() // Parsed items of Accept-Encoding header
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

## Receiving the payload of the request
{: #receiving}

`POST`, `PUT` and `PATCH` requests has an associated payload.
That payload is usually encoded.

To access the raw bits of the payload, you can use `receiveChannel`, but it is
directly part of the `call` instead of `call.request`:

```kotlin
val channel: ByteReadChannel = call.receiveChannel()
```

The call also supports receiving generic objects:

```kotlin
val obj: T = call.receive<T>()
val obj: T? = call.receiveOrNull<T>()
```

And it provide some convenience methods for common types:

```kotlin
val channel: ByteReadChannel = call.receiveChannel()
val text: String = call.receiveText()
val inputStream: InputStream = call.receiveStream() // NOTE this is synchronous
val multipart: MultiPartData = call.receiveMultipart()
```

To parse a form urlencoded or with multipart, you can use `receiveParameters`:

```kotlin
val postParameters: Parameters = call.receiveParameters()
```

## Receiving custom objects, ContentNegotiation and JSON
{: #receiving-content-negotitation}

In order to receive custom objects from the payload,
you have to use the `ContentNegotiation` feature.
This is useful for example to receive and send JSON payloads in REST APIs.  

```kotlin
install(ContentNegotiation) {
    gson {
        setDateFormat(DateFormat.LONG)
        setPrettyPrinting()
    }
}
```

In the case you configure the ContentNegotiation to use gson,
you will need to include the `ktor-gson` artifact:

```kotlin
compile "io.ktor:ktor-gson:$ktor_version"
```

Then you can do, as an example:

```kotlin
data class HelloWorld(val hello: String)

routing {
    post("/route") {
        val helloWorld = call.receive<HelloWorld>()
    }
}
```

Remember that your classes must be defined top level (outside any other class or function) to be recognized by Gson. 
{: .note}
