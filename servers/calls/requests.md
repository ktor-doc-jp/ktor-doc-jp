---
title: Requests
caption: Handling HTTP Requests  
category: servers
permalink: /servers/calls/requests.html
keywords: multipart receiving
priority: 800
redirect_from:
  - /servers/requests.html
---

When handling routes, or directly intercepting the pipeline, you
get a context with an [ApplicationCall](/servers/calls.html).
That `call` contains a property called `request` that includes information about the request.

Also, the call itself has some useful convenience properties and methods that rely on the request.

**Table of contents:**

* TOC
{:toc}

## Context
{: #context}

When using the [Routing](/servers/features/routing.html) feature, you can access
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

As part of the `request`, you can get access to its internal context:

```kotlin
val call: ApplicationCall = request.call
val pipeline: ApplicationReceivePipeline = request.pipeline
```

You can also access the information about the connection point (either local, or original
if under a proxy (if the [`XForwardedHeaderSupport` feature](/servers/features/forward-headers.html) is installed),
and the proxy sent header information):

* `val local : RequestConnectionPoint = request.local` - local information 
* `val origin: RequestConnectionPoint = request.origin` - local / using proxy headers if [`XForwardedHeaderSupport` feature](/servers/features/forward-headers.html) is installed.
* `val uri: String = request.uri` - Short cut for `origin.uri`
* `val document: String = request.document()` - The last component after '/' of the uri
* `val path: String = request.path()` - The uri without the query string
* `val host: String? = request.host()` - The host part without the port 
* `val port: Int = request.port()` - Port of request

```kotlin
interface RequestConnectionPoint {
    val scheme: String // HTTP/HTTPS: The provided protocol (local) or `X-Forwarded-Proto`
    val version: String
    val port: Int
    val host: String // The provided host (local) or `X-Forwarded-Host`
    val uri: String
    val method: HttpMethod
    val remoteHost: String // The client IP (the direct ip for `local`, or the redirected one `X-Forwarded-For`)
}
```

You can access the `HttpMethod` and the `httpVersion` used for the request:

* `val httpMethod: HttpMethod = request.httpMethod`
* `val httpVersion: String = request.httpVersion`

If you need to access the query parameters `?param1=value&param2=value` as a collection,
you can use `queryParameters`. The same happens with `headers: Headers`. Both types
implement the `StringValues` interface where each key can have a list of Strings associated with it.

* `val queryParameters: Parameters = request.queryParameters`
* `val queryString: String = request.queryString()`
* `val headers: Headers = request.headers`

* `val param1: String? = request.queryParameters["param1"]`
* `val repeatedParam: List<String>? = request.queryParameters.getAll("repeatedParam")`

There is a `cookies` property to access the `Cookie` headers sent by the client,
just as if it was a collection:

* `val cookies: RequestCookies = request.cookies`
* `val mycookie: String? = request.cookies["mycookie"]`

And several convenience methods to access headers:

* `val header: String? = request.header("HeaderName")`
* `val contentType: ContentType = request.contentType()` - Parsed Content-Tpe 
* `val contentCharset: Charset? = request.contentCharset()` - Content-Type JVM charset
* `val authorization: String? = request.authorization()` - Authorization header
* `val location: String? = request.location()` - Location header
* `val accept: String? = request.accept()` - Accept header
* `val acceptItems: List<HeaderValue> = request.acceptItems()` - Parsed items of Accept header
* `val acceptEncoding: String? = request.acceptEncoding()` - Accept-Encoding header
* `val acceptEncodingItems: List<HeaderValue> = request.acceptEncodingItems()` - Parsed items of Accept-Encoding header
* `val acceptLanguage: String? = request.acceptLanguage()` - Accept-Language header
* `val acceptLanguageItems: List<HeaderValue> = request.acceptLanguageItems()` - Parsed Accept-Language items
* `val acceptCharset: String? = request.acceptCharset()` - Accept-Charset header
* `val acceptCharsetItems: List<HeaderValue> = request.acceptCharsetItems()` - Parsed Accept-Charset items
* `val userAgent: String? = request.userAgent()` - User-Agent header
* `val cacheControl: String? = request.cacheControl()` - Cache-Control header
* `val ranges: RangesSpecifier? = request.ranges()` - Parsed Ranges header

* `val isChunked: Boolean = request.isChunked()` - Transfer-Encoding: chunked
* `val isMultipart: Boolean = request.isMultipart()` - Content-Type matches Multipart

## Receiving the payload of the request
{: #receiving}

`POST`, `PUT` and `PATCH` requests has an associated payload.
That payload is usually encoded.

To access the raw bits of the payload, you can use `receiveChannel`, but it is
directly part of the `call` instead of `call.request`:

* `val channel: ByteReadChannel = call.receiveChannel()`

The call also supports receiving generic objects:

* `val obj: T = call.receive<T>()`
* `val obj: T? = call.receiveOrNull<T>()`

And it provide some convenience methods for common types:

* `val channel: ByteReadChannel = call.receiveChannel()`
* `val text: String = call.receiveText()`
* `val inputStream: InputStream = call.receiveStream()` - NOTE that the InputStream API is synchronous and will block
* `val multipart: MultiPartData = call.receiveMultipart()`

To parse a form urlencoded or with multipart, you can use `receiveParameters` or `receive<Parameters>`:

```kotlin
val postParameters: Parameters = call.receiveParameters()
```

All those receive* methods are aliases of `call.receive<T>` with the specified type.
The types `ByteReadChannel`, `ByteArray`, `InputStream`, `MultiPartData`, `String` and `Parameters` are handled by
`ApplicationReceivePipeline.installDefaultTransformations` that is installed by default.

You can create custom transformers by calling
`application.receivePipeline.intercept(ApplicationReceivePipeline.Transform) { query ->`
and then calling `proceedWith(ApplicationReceiveRequest(query.type, transformed))` as does the [ContentNegotiation feature](/servers/features/content-negotiation.html).

All the receive methods consume the payload sent by the client.
They won't throw an exception if you call them several times,
but subsequent receives will be executed with an empty payload.
So for example, `receiveText` would return an empty string starting with the second call.
{: .note #receiving-several-times}

## Receiving files and multipart
{: #receiving-files}

Check the [uploads](/servers/uploads.html) section.

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

If you configure the ContentNegotiation to use gson,
you will need to include the `ktor-gson` artifact:

```kotlin
compile("io.ktor:ktor-gson:$ktor_version")
```

Then you can, as an example, do:

```kotlin
data class HelloWorld(val hello: String)

routing {
    post("/route") {
        val helloWorld = call.receive<HelloWorld>()
    }
}
```

Remember that your classes must be defined top level (outside of any other class or function) to be recognized by Gson. 
{: .note #receiving-gson-top-level}
