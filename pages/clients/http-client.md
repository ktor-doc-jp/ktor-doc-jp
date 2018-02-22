---
title: Http Client
section: Clients
permalink: /clients/http-client.html
caption: Http Client 
---

* TOC
{:toc}

In addition to the HTTP server, Ktor also supports flexible asynchronous HTTP Clients.

## Basic Usage

The basic usage is super simple. You just have to instantiate an `HttpClient` instance,
specifying an engine, for example [`Apache`](#apache), [`Jetty`](#jetty)
or [`CIO`](#cio).

Then you can start making requests. It is possible to customize the request a lot and
to stream the request and response payloads , but you can also just call a convenience
extension method like `HttpClient.get` that will perform a `GET` request and will receive
an specified type directly (for example `String`).  

Perform a `GET` request fully reading a `String`:

```kotlin
val client = HttpClient(Apache)
val htmlContent = client.get<String>("https://en.wikipedia.org/wiki/Main_Page")
```

Perform a `GET` request fully reading a `ByteArray`:

```kotlin
val client = HttpClient(Apache)
val bytes = client.call("http://127.0.0.1:8080/").response.readBytes()
```

## Complete Usage

### The `call` method

The HttpClient `call` method, returns an `HttpClientCall` and allows you to perform
simple untyped requests.

You can read the content by using `response: HttpResponse`.
More information about [receiving content using HttpResponse here](#HttpResponse). 

```kotlin
val call = client.call("http://127.0.0.1:8080/") {
    method = HttpMethod.Get
}
println(call.response.readText())
```

### The `request` method

In addition to call, there is a `request` method for performing a typed request,
[receiving a specific type](#receive) like String, HttpResponse, or an arbitrary class.
You have to specify the URL, and the method when building the request. 

```kotlin
val call = client.request<String> {
    url(URL("http://127.0.0.1:8080/"))
    method = HttpMethod.Get
}
```

### `post` and `get` methods

Similar to request there are several extension methods to perform requests
with the specific more common HTTP verbs `GET` and `POST`.

```kotlin
val text = client.post<String>("http://127.0.0.1:8080/")
```

### Specifying a body for requests

By default, for `POST` and `PUT` requests, you can set the `body` property
of a `HttpRequestBuilder` to any sub-instance of `OutgoingContent`
as well as String instances.

```kotlin
client.post<Unit> {
    url(URL("http://127.0.0.1:8080/"))
    body = "HELLO WORLD!"
    //body = TextContent("HELLO WORLD!", ContentType.Text.Plain)
    //body = ByteArrayContent("HELLO WORLD!".toByteArray(Charsets.UTF_8))
    //body = LocalFileContent(File("build.gradle"))
    //body = JarFileContent(File("myjar.jar"), "test.txt", ContentType.fromFileExtension("txt").first())
    //body = URIFileContent(URL("https://en.wikipedia.org/wiki/Main_Page"))

}
```

If you install `JsonFeature`, and set the content type to `application/json`
you can use arbitrary instances, and they will be serialized as JSON.

**Note:** Remember that your classes must be top level to be recognized by Gson.
If you put a class you want to be serialized inside a function, it will send a null.

```kotlin
data class HelloWorld(val hello: String) // Must be top level

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

<a id="receive"></a>
### Receiving the body of a response

By default you can use `HttpResponse` or `String` as possible types for typed
HttpClient requests. So for example:

```kotlin
val htmlContent = client.get<String>("https://en.wikipedia.org/wiki/Main_Page")
val response = client.get<HttpResponse>("https://en.wikipedia.org/wiki/Main_Page")
```

If JsonFeature is configured and the server returns the `Content-Type: application/json`,
you can also specify a class for deserializing it.

```kotlin
val helloWorld = client.get<HelloWorld>("http://127.0.0.1:8080/")
```

<a id="HttpResponse"></a>
#### The `HttpResponse` class

From an `HttpResponse`, you can get the response content easily:
 
```kotlin
val bytes: ByteArray = response.readBytes()
val text: String = response.readText()
val readChannel = response.receive<ByteReadChannel>()
val multiPart = response.receive<MultiPartData>()
val inputStream = response.receive<InputStream>() // This is synchronous!
response.discardRemaining()
```

You can also get additional response information (status, headers, internal state...):

```kotlin
// Basic
val status: HttpStatusCode = response.status
val headers: Headers = response.headers

// Advanced
val call: HttpClientCall = response.call
val version: HttpProtocolVersion = response.version
val requestTime: Date = response.requestTime 
val responseTime: Date = response.responseTime
val executionContext: Job = response.executionContext

// Extensions for headers:
val contentType: ContentType? = response.contentType()
val charset: Charset? = response.charset()
val lastModified: Date? = response.lastModified()
val etag: String? = response.etag()
val expires: Date? = response.expires()
val vary: List<String>? = response.vary()
val contentLength: Int? = response.contentLength()
val setCookie: List<Cookie> = response.setCookie()
````

## Configuring the request with `HttpRequestBuilder`

When calling request methods, you can provide a lambda to build the request
parameters like the URL, the HTTP method (verb), the body, or the headers.
The HttpRequestBuilder looks like this:


```kotlin
class HttpRequestBuilder : HttpMessageBuilder {
    val url: URLBuilder
    var method: HttpMethod
    val headers: HeadersBuilder
    var body: Any = EmptyContent
    val executionContext: CompletableDeferred<Unit>
    fun headers(block: HeadersBuilder.() -> Unit)
    fun url(block: URLBuilder.(URLBuilder) -> Unit)
}
```

## Http Client Features

Similar to the server, Ktor supports features on the client. It also works the same:
there is a pipeline for client HTTP requests and features can intercept them.

### BasicAuth

Feature to send an `Authorization: Basic` with the specified credentials.

```kotlin
val client = HttpClient(HttpClientEngine) {
    install(BasicAuth) {
        username = "username"
        password = "password"
    }
}
```

**Note:** To use this feature, you need to include the `ktor-client-auth-basic` artifact.

### HttpCookies

There is a feature to keep cookies between calls or to make requests with some cookies pre-set.

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

Discards the body of the response.

```kotlin
val client = HttpClient(HttpClientEngine) {
    install(HttpIgnoreBody)
}
```

### HttpPlainText

Processes the request content as plain text of a specified charset by
`defaultCharset`, note that the default charset is the JVM's charset that
could be different between systems.
Also it will process the response content as plain text too.

```kotlin
val client = HttpClient(HttpClientEngine) {
    install(HttpPlainText) {
        defaultCharset = Charsets.UTF_8
    }
}
```

### JsonFeature

Processes the request and the response payload as JSON, serializing
and de-serializing them using a specific `serializer: JsonSerializer`.

```kotlin
val client = HttpClient(HttpClientEngine) {
    install(JsonSerializer) {
        serializer = GsonSerializer()
    }
}
```

**Note:** To use this feature, you need to include `io.ktor:ktor-client-json` artifact.

<a id="engines"></a>
## Supported engines and configuration

Ktor HttpClient lets you to configure the parameters of the engine by calling `Engine.config { }`.

### Common

Every engine config has two common properties that can be set:

```kotlin
val client = HttpClient(MyHttpEngine.config {
    dispatcher = HTTP_CLIENT_DEFAULT_DISPATCHER
    sslContext = SSLContext.getDefault()
})
```

* The `dispatcher` property is the `CoroutineDispatcher` used when processing client requests.
* While the `sslContext` is from Java [`SSLContext`](https://docs.oracle.com/javase/7/docs/api/javax/net/ssl/SSLContext.html)
allowing you to set custom keys, trust manager or custom source for secure random data.

<a id="apache"></a>
### Apache

Apache is the most configurable HTTP client at this point.
It is the only one that supports following redirects and allows you to configure timeouts,
proxies among other things supported by `org.apache.httpcomponents:httpasyncclient`.

* Artifact `io.ktor:ktor-client-apache:$ktor_version`.
* Transitive dependency: `org.apache.httpcomponents:httpasyncclient:4.1.3`.
* Supports HTTP/1.1 and HTTP/2.

```kotlin
val client = HttpClient(Apache.config {
    followRedirects = true  // Follow http Location redirects - default false. It uses the default number of redirects defined by Apache's HttpClient that is 50.
    
    // For timeouts: 0 means infinite, while negative value mean to use the system's default value
    socketTimeout = 10_000  // Max time between TCP packets - default 10 seconds
    connectTimeout = 10_000 // Max time to establish an HTTP connection - default 10 seconds
    connectionRequestTimeout = 20_000 // Max time for the connection manager to start a request - 20 seconds
    
    customizeClient {
        // Apache's HttpAsyncClientBuilder
        setProxy(HttpHost("127.0.0.1", 8080))
    }
    customizeRequest {
        // Apache's RequestConfig.Builder
    }
})
```

<a id="cio"></a>
### CIO

CIO provides `maxConnectionsCount` and a `endpointConfig` for configuring.

* Artifact `io.ktor:ktor-client-cio:$ktor_version`.
* No additional transitive dependencies.
* Only supports HTTP/1.x for now.

```kotlin
fun test() {
    HttpClient(CIO.config { 
        maxConnectionsCount = 1000 // Maximum number of socket connections.
        endpointConfig = EndpointConfig().apply {
            maxConnectionsPerRoute = 100 // Maximum number of requests for a specific endpoint route.
            pipelineMaxSize = 20 // Max number of opened endpoints.
            keepAliveTime = 5000 // Max number of milliseconds to keep each connection alive.
            connectTimeout = 5000 // Number of milliseconds to wait trying to connect to the server.
            connectRetryAttempts = 5 // Maximum number of attempts for retrying a connection.
        }
    })
}
```

<a id="jetty"></a>
### Jetty

Jetty provides an additional `sslContextFactory` for configuring.

* Artifact `io.ktor:ktor-client-jetty:$ktor_version`
* Transitive dependency: `org.eclipse.jetty.http2:http2-client:9.4.8.v20171121`
* Just supports HTTP/2 for now.

```kotlin
fun test() {
    HttpClient(Jetty.config { 
        sslContextFactory = SslContextFactory()
    })
}
```

## Concurrent requests

Remember that requests are asynchronous, but when performing a requests, the API is suspending
and your function will be suspended until done. If you want to perform several requests at once
in the same block, you can use `launch` or `async` functions and later get the results.
For example:

**Sequential requests:**

```kotlin
suspend fun mySuspendFunc() {
    val client = HttpClient(Apache)
    
    // Get the content of an URL.
    val bytes1 = client.call("https://127.0.0.1:8080/a").response.readBytes() // Suspension point.
    
    // Once the previous request is done, get the content of an URL.
    val bytes2 = client.call("https://127.0.0.1:8080/b").response.readBytes() // Suspension point.
}
```

**Parallel requests:**

```kotlin
suspend fun mySuspendFunc() {
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

## Client Pipeline Phases

### HttpRequestPipeline

```kotlin
/**
 * All interceptors accept payload as [subject] and try to convert it to [OutgoingContent]
 * Last phase should proceed with [HttpClientCall]
 */
companion object Phases {
    val Before = PipelinePhase("Before") // The earliest phase that happens before any other
    val State = PipelinePhase("State") // Use this phase to modify request with shared state
    val Transform = PipelinePhase("Transform") // Transform request body to supported render format
    val Render = PipelinePhase("Render") // Encode request body to [OutgoingContent]
    val Send = PipelinePhase("Send") // Send request to remote server
}
```

### HttpResponsePipeline

```kotlin
companion object Phases {
    val Receive = PipelinePhase("Receive") // The earliest phase that happens before any other
    val Parse = PipelinePhase("Parse") // Decode response body
    val Transform = PipelinePhase("Transform") // Transform response body to expected format
    val State = PipelinePhase("State") // Use this phase to store request shared state
    val After = PipelinePhase("After") // Latest response pipeline phase
}
```

## Examples

### Interchanging JSON: Ktor server / Ktor client

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
