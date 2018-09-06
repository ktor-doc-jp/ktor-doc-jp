---
title: DefaultRequest
category: clients
caption: DefaultRequest
feature:
  artifact: io.ktor:ktor-client-core:$ktor_version
  class: io.ktor.client.features.DefaultRequest
  method: io.ktor.client.features.defaultRequest
---

This feature allows you to configure some defaults for all the requests for a specific client.  

{% include feature.html %}

### Installation

When configuring the client, there is an extension method provided by this feature to set come defaults for this client. 

```kotlin
val client = HttpClient(engine).config {
    defaultRequest { // this: HttpRequestBuilder ->
        method = HttpMethod.Head
        host = "127.0.0.1"
        port = 8080
    }
}
```

### Example

An example showing how to the client behaves using the [MockEngine](/clients/http-client/testing.html):

```kotlin
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.io.*

fun main(args: Array<String>) {
    runBlocking {
        val engine: HttpClientEngine = MockEngine { call ->
            MockHttpResponse(
                call,
                HttpStatusCode.OK,
                ByteReadChannel(
                    "method: $method, host: ${url.host}, port: ${url.port}, fullPath: ${url.fullPath}"
                        .toByteArray(Charsets.UTF_8)
                ),
                headersOf("Content-Type" to listOf(ContentType.Text.Plain.toString()))
            )
        }
        val client = HttpClient(engine).config {
            defaultRequest {
                method = HttpMethod.Head
                host = "127.0.0.1"
                port = 8080
            }
        }
        val result = client.get<String> { url { encodedPath = "/demo" } }
        println(result)
        
        // Prints: method: HttpMethod(value=HEAD), host: 127.0.0.1, port: 8080, fullPath: /demo
    }
}

```