---
title: Http Client
category: clients
permalink: /clients/http-client.html
caption: Http Client 
---

{::options toc_levels="1..2" /}

In addition to HTTP serving, Ktor also includes a flexible asynchronous HTTP client.
This client supports several [configurable engines](#engines), and has its own set of [features](#features).

The main functionality is available through the `io.ktor:ktor-client-core:$ktor_version` artifact.
And each engine, is provided in [separate artifacts](#engines).
{: .note.artifact }

**Table of contents:**

* TOC
{:toc}

## Simple requests

The basic usage is *super* simple: you just have to instantiate an `HttpClient` instance,
specifying an engine, for example [`Apache`](#apache), [`Jetty`](#jetty)
or [`CIO`](#cio), and start making requests using one of the many convenience methods available.

First you need to instantiate the client:   

```kotlin
val client = HttpClient(Apache)
```

Then, to perform a `GET` request fully reading a `String`:

```kotlin
val htmlContent = client.get<String>("https://en.wikipedia.org/wiki/Main_Page")
```

And in the case you are interested in the raw bits, you can read a `ByteArray`:

```kotlin
val bytes: ByteArray = client.call("http://127.0.0.1:8080/").response.readBytes()
```

It is possible to customize the request a lot and
to stream the request and response payloads, but you can also just call a convenience
extension method like `HttpClient.get` to do a `GET` request to receive
the specified type directly (for example `String`).
{: .note}

## Custom requests

We cannot live only on *get* requests, Ktor allows you to build complex
requests with any of the HTTP verbs, with the flexibility to process responses in many ways.

### The `call` method

The HttpClient `call` method, returns an `HttpClientCall` and allows you to perform
simple untyped requests.

You can read the content using `response: HttpResponse`.
For further information, check out the [receiving content using HttpResponse](#HttpResponse) section. 

```kotlin
val call = client.call("http://127.0.0.1:8080/") {
    method = HttpMethod.Get
}
println(call.response.readText())
```

### The `request` method

In addition to call, there is a `request` method for performing a typed request,
[receiving a specific type](#receive) like String, HttpResponse, or an arbitrary class.
You have to specify the URL and the method when building the request. 

```kotlin
val call = client.request<String> {
    url(URL("http://127.0.0.1:8080/"))
    method = HttpMethod.Get
}
```

### The `post` and `get` methods

Similar to `request`, there are several extension methods to perform requests
with the most common HTTP verbs: `GET` and `POST`.

```kotlin
val text = client.post<String>("http://127.0.0.1:8080/")
```

When calling request methods, you can provide a lambda to build the request
parameters like the URL, the HTTP method (verb), the body, or the headers.
The `HttpRequestBuilder` looks like this:

```kotlin
class HttpRequestBuilder : HttpMessageBuilder {
    val url: URLBuilder
    var method: HttpMethod
    val headers: HeadersBuilder
    var body: Any = EmptyContent
    val executionContext: CompletableDeferred<Unit>
    fun header(key: String, value: String)
    fun headers(block: HeadersBuilder.() -> Unit)
    fun url(block: URLBuilder.(URLBuilder) -> Unit)
}
```

The `HttpClient` class only offers some basic functionality, and all the methods for building requests are exposed as extensions.\\
You can check the standard available [HttpClient build extension methods](https://github.com/ktorio/ktor/blob/master/ktor-client/ktor-client-core/src/io/ktor/client/request/builders.kt).
{: .note.api}

### Specifying custom headers
{: #custom-headers}

When building requests with `HttpRequestBuilder`, you can set custom headers.
There is a final property `val headers: HeadersBuilder` that inherits from `StringValuesBuilder`.
You can add or remove headers using it, or with the `header` convenience methods.

```kotlin
// this : HttpMessageBuilder

// Convenience method to add a header
header("My-Custom-Header", "HeaderValue")

// Calls methods from the headers: HeadersBuilder to manipulate the headers
headers.clear()
headers.append("My-Custom-Header", "HeaderValue")
headers.appendAll("My-Custom-Header", listOf("HeaderValue1", "HeaderValue2"))
headers.remove("My-Custom-Header")

// Applies the headers with the `headers` convenience method
headers { // this: HeadersBuilder
    clear()
    append("My-Custom-Header", "HeaderValue")
    appendAll("My-Custom-Header", listOf("HeaderValue1", "HeaderValue2"))
    remove("My-Custom-Header")
}
``` 


## Specifying a body for requests

For `POST` and `PUT` requests, you can set the `body` property:

```kotlin
client.post<Unit> {
    url(URL("http://127.0.0.1:8080/"))
    body = // ...
}
```

The `HttpRequestBuilder.body` property can be a subtype of `OutgoingContent` as well as a `String` instance:

* `body = "HELLO WORLD!"`
* `body = TextContent("HELLO WORLD!", ContentType.Text.Plain)`
* `body = ByteArrayContent("HELLO WORLD!".toByteArray(Charsets.UTF_8))`
* `body = LocalFileContent(File("build.gradle"))`
* `body = JarFileContent(File("myjar.jar"), "test.txt", ContentType.fromFileExtension("txt").first())`
* `body = URIFileContent(URL("https://en.wikipedia.org/wiki/Main_Page"))`

If you install the *JsonFeature*, and set the content type to `application/json`
you can use arbitrary instances as the `body`, and they will be serialized as JSON:

```kotlin
data class HelloWorld(val hello: String)

val client = HttpClient(Apache) {
    install(JsonFeature) {
        serializer = GsonSerializer {
            // Configurable .GsonBuilder
            serializeNulls()
            disableHtmlEscaping()
        }
    }
}

client.post<Unit> {
    url(URL("http://127.0.0.1:8080/"))
    contentType(ContentType.Application.Json) // Required
    body = HelloWorld(hello = "world")
}
```

Remember that your classes must be *top-level* to be recognized by `Gson`. \\
If you try to send a class that is inside a function, the feature will send a *null*.
{: .note}

## Receiving the body of a response
{: #receive}

By default you can use `HttpResponse` or `String` as possible types for typed
HttpClient requests. So for example:

```kotlin
val htmlContent = client.get<String>("https://en.wikipedia.org/wiki/Main_Page")
val response = client.get<HttpResponse>("https://en.wikipedia.org/wiki/Main_Page")
```

If *JsonFeature* is configured, and the server returns the header `Content-Type: application/json`,
you can also specify a class for deserializing it.

```kotlin
val helloWorld = client.get<HelloWorld>("http://127.0.0.1:8080/")
```

#### The `HttpResponse` class
{: #HttpResponse }

From an `HttpResponse`, you can get the response content easily:
 
* `val readChannel: ByteReadChannel = response.content`
* `val bytes: ByteArray = response.readBytes()`
* `val text: String = response.readText()`
* `val readChannel = response.call.receive<ByteReadChannel>()`
* `val multiPart = response.call.receive<MultiPartData>()`
* `val inputStream = response.call.receive<InputStream>()` *Remember that InputStream API is synchronous!*
* `response.discardRemaining()`

You can also get the additional response information such as its status, headers, internal state, etc.:

*Basic*:

* `val status: HttpStatusCode = response.status`
* `val headers: Headers = response.headers`

*Advanced*:
* `val call: HttpClientCall = response.call`
* `val version: HttpProtocolVersion = response.version`
* `val requestTime: Date = response.requestTime`
* `val responseTime: Date = response.responseTime`
* `val executionContext: Job = response.executionContext`

*Extensions for headers*:
* `val contentType: ContentType? = response.contentType()`
* `val charset: Charset? = response.charset()`
* `val lastModified: Date? = response.lastModified()`
* `val etag: String? = response.etag()`
* `val expires: Date? = response.expires()`
* `val vary: List<String>? = response.vary()`
* `val contentLength: Int? = response.contentLength()`
* `val setCookie: List<Cookie> = response.setCookie()`

## Features
{: #features}

Similar to the server, Ktor supports features on the client. And it has the same design:
there is a pipeline for client HTTP requests, and there are interceptors and installable features.

### BasicAuth

This feature sends an `Authorization: Basic` with the specified credentials:

```kotlin
val client = HttpClient(HttpClientEngine) {
    install(BasicAuth) {
        username = "username"
        password = "password"
    }
}
```

To use this feature, you need to include the `ktor-client-auth-basic` artifact.
{: .note.artifact }

This feature implements the IETF's [RFC 7617](https://tools.ietf.org/html/rfc7617).
{: .note}

### HttpCookies

This feature keeps cookies between calls or forces specific cookies:

```kotlin
val client = HttpClient(HttpClientEngine) {
    install(HttpCookies) {
        // Will keep an in-memory map with all the cookies from previous requests.
        storage = AcceptAllCookiesStorage()
        
        // Will ignore Set-Cookie and will send the specified cookies.
        storage = ConstantCookieStorage(Cookie("mycookie1", "value"), Cookie("mycookie2", "value"))
    }
}
client.cookies("mydomain.com")
```

### HttpIgnoreBody

This feature discards the body of the response:

```kotlin
val client = HttpClient(HttpClientEngine) {
    install(HttpIgnoreBody)
}
```

Use this if you are only interested in the response headers, and you cannot use the HEAD verb.
This will use less memory and will execute faster.
{: .note}

### HttpPlainText

This feature processes the request content as plain text of a specified charset by `defaultCharset`.
Also, it will process the response content as plain text too.

```kotlin
val client = HttpClient(HttpClientEngine) {
    install(HttpPlainText) {
        defaultCharset = Charsets.UTF_8
    }
}
```

Bear in mind that the default charset is the JVM's charset that could be different between systems. \\
That's why it is recommended to specify the default charset.
{: .note}

### JsonFeature

Processes the request and the response payload as JSON, serializing
and de-serializing them using a specific `serializer: JsonSerializer`.

```kotlin
val client = HttpClient(HttpClientEngine) {
    install(JsonFeature) {
        serializer = GsonSerializer()
    }
}
```

To use this feature, you need to include `io.ktor:ktor-client-json` artifact.
{: .note.artifact }

### WebSockets

This feature enables bi-directional WebSocket connections with the server.
You can read more about it in its [dedicate WebSockets page](/clients/websockets.html). 

To use this feature, you need to include `io.ktor:ktor-client-websocket` artifact.
{: .note.artifact }

### Creating Custom Features

If you want to create features, you can use the [standard features](https://github.com/ktorio/ktor/tree/master/ktor-client/ktor-client-core/src/io/ktor/client/features) as a reference.

You can also check the [HttpRequestPipeline.Phases](https://github.com/ktorio/ktor/blob/master/ktor-client/ktor-client-core/src/io/ktor/client/request/HttpRequestPipeline.kt)
and [HttpResponsePipeline.Phases](https://github.com/ktorio/ktor/blob/master/ktor-client/ktor-client-core/src/io/ktor/client/response/HttpResponsePipeline.kt)
to understand the interception points available.
{: .note.tip}

## Supported engines
{: #engines}

Ktor HttpClient lets you configure the parameters of each engine by calling `Engine.config { }`.

Every engine config has two common properties that can be set:

* The `dispatcher` property is the `CoroutineDispatcher` used when processing client requests.
* The `sslContext` is a [`javax.net.ssl.SSLContext`](https://docs.oracle.com/javase/7/docs/api/javax/net/ssl/SSLContext.html)
allowing you to set custom keys, a trust manager or custom source for secure random data.

```kotlin
val client = HttpClient(MyHttpEngine.config {
    dispatcher = HTTP_CLIENT_DEFAULT_DISPATCHER
    sslContext = SSLContext.getDefault()
})
```

You can also adjust maximum total connections and maximum connections
per route in Apache and CIO clients (but not Jetty).

### Apache
{: #apache}

Apache is the most configurable HTTP client about right now. It supports HTTP/1.1 and HTTP/2.
It is the only one that supports following redirects and allows you to configure timeouts,
proxies among other things it is supported by `org.apache.httpcomponents:httpasyncclient`.

A sample configuration would look like:

```kotlin
val client = HttpClient(Apache.config {
    followRedirects = true  // Follow HTTP Location redirects - default false. It uses the default number of redirects defined by Apache's HttpClient that is 50.
    
    // For timeouts: 0 means infinite, while negative value mean to use the system's default value
    socketTimeout = 10_000  // Max time between TCP packets - default 10 seconds
    connectTimeout = 10_000 // Max time to establish an HTTP connection - default 10 seconds
    connectionRequestTimeout = 20_000 // Max time for the connection manager to start a request - 20 seconds
    
    customizeClient {
        // Apache's HttpAsyncClientBuilder
        setProxy(HttpHost("127.0.0.1", 8080))
        setMaxConnTotal(1000) // Maximum number of socket connections.
        setMaxConnPerRoute(100) // Maximum number of requests for a specific endpoint route.
    }
    customizeRequest {
        // Apache's RequestConfig.Builder
    }
})
```
{: .compact}


Artifact `io.ktor:ktor-client-apache:$ktor_version`.\\
Transitive dependency: `org.apache.httpcomponents:httpasyncclient:4.1.3`.
{: .note.artifact }

### CIO
{: #cio}

CIO (Coroutine-based I/O) is a Ktor implementation with no additional dependencies and is fully asynchronous.
It only supports HTTP/1.x for now.

CIO provides `maxConnectionsCount` and a `endpointConfig` for configuring.

A sample configuration would look like:

```kotlin
val client = HttpClient(CIO.config { 
    maxConnectionsCount = 1000 // Maximum number of socket connections.
    endpoint.apply {
        maxConnectionsPerRoute = 100 // Maximum number of requests for a specific endpoint route.
        pipelineMaxSize = 20 // Max number of opened endpoints.
        keepAliveTime = 5000 // Max number of milliseconds to keep each connection alive.
        connectTimeout = 5000 // Number of milliseconds to wait trying to connect to the server.
        connectRetryAttempts = 5 // Maximum number of attempts for retrying a connection.
    }
})
```
{: .compact}

Artifact `io.ktor:ktor-client-cio:$ktor_version`.\\
No additional transitive dependencies.
{: .note.artifact }

### Jetty
{: .jetty}

Jetty provides an additional `sslContextFactory` for configuring. It only supports HTTP/2 for now.

A sample configuration would look like:

```kotlin
val client = HttpClient(Jetty.config { 
    sslContextFactory = SslContextFactory()
})
```

Artifact `io.ktor:ktor-client-jetty:$ktor_version`. \\
Transitive dependency: `org.eclipse.jetty.http2:http2-client:9.4.8.v20171121`.
{: .note.artifact }

## Concurrency

Remember that requests are asynchronous, but when performing requests, the API suspends further requests
and your function will be suspended until done. If you want to perform several requests at once
in the same block, you can use `launch` or `async` functions and later get the results.
For example:

*Sequential requests:*

```kotlin
suspend fun sequentialRequests() {
    val client = HttpClient(Apache)
    
    // Get the content of an URL.
    val bytes1 = client.call("https://127.0.0.1:8080/a").response.readBytes() // Suspension point.
    
    // Once the previous request is done, get the content of an URL.
    val bytes2 = client.call("https://127.0.0.1:8080/b").response.readBytes() // Suspension point.
}
```

*Parallel requests:*

```kotlin
suspend fun parallelRequests() {
    val client = HttpClient(Apache)
    
    // Start two requests asynchronously.
    val req1 = async { client.call("https://127.0.0.1:8080/a").response.readBytes() }
    val req2 = async { client.call("https://127.0.0.1:8080/b").response.readBytes() }
    
    // Get the request contents without blocking threads, but suspending the function until both
    // requests are done.
    val bytes1 = req1.await() // Suspension point.
    val bytes2 = req2.await() // Suspension point.
}
```

## Examples

### Interchanging JSON: Ktor server / Ktor client
{: #example-json }

```kotlin
fun main(args: Array<String>) {
    val server = embeddedServer(
        Netty,
        port = 8080,
        module = Application::mymodule
    ).apply {
        start(wait = false)
    }

    runBlocking {
        val client = HttpClient(Apache) {
            install(JsonFeature) {
                serializer = GsonSerializer {
                    // .GsonBuilder
                    serializeNulls()
                    disableHtmlEscaping()
                }
            }
        }

        val message = client.post<HelloWorld> {
            url(URL("http://127.0.0.1:8080/"))
            contentType(ContentType.Application.Json)
            body = HelloWorld(hello = "world")
        }

        println("CLIENT: Message from the server: $message")

        client.close()
        server.stop(1L, 1L, TimeUnit.SECONDS)
    }
}

data class HelloWorld(val hello: String)

fun Application.mymodule() {
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
    routing {
        post("/") {
            val message = call.receive<HelloWorld>()
            println("SERVER: Message from the client: $message")
            call.respond(HelloWorld(hello = "response"))
        }
    }
}
```

You can check the [ktor-samples](https://github.com/ktorio/ktor-samples) and [ktor-exercises](https://github.com/ktorio/ktor-exercises) repositories for samples and exercises.
{: .note }
