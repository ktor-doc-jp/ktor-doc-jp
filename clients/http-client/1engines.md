---
title: Engines
category: clients
permalink: /clients/http-client/engines.html
caption: HTTP Client Engines 
---

## Supported engines
{: #engines}

Ktor HttpClient lets you configure the parameters of each engine by calling `Engine.config { }`, but since 0.9.4,
the preferred way is to use `HttpClient(MyHttpEngine) { engine { ... } }` instead.

Every engine config has two common properties that can be set:

* The `dispatcher` property is the `CoroutineDispatcher` used when processing client requests.
* The `sslContext` is a [`javax.net.ssl.SSLContext`](https://docs.oracle.com/javase/7/docs/api/javax/net/ssl/SSLContext.html)
allowing you to set custom keys, a trust manager or custom source for secure random data.

```kotlin
val client = HttpClient(MyHttpEngine) {
    engine {
        dispatcher = HTTP_CLIENT_DEFAULT_DISPATCHER
        sslContext = SSLContext.getDefault()
    }
}
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
val client = HttpClient(Apache){
    engine {
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
    }
}
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
val client = HttpClient(CIO){
    engine {
        maxConnectionsCount = 1000 // Maximum number of socket connections.
        endpoint.apply {
            maxConnectionsPerRoute = 100 // Maximum number of requests for a specific endpoint route.
            pipelineMaxSize = 20 // Max number of opened endpoints.
            keepAliveTime = 5000 // Max number of milliseconds to keep each connection alive.
            connectTimeout = 5000 // Number of milliseconds to wait trying to connect to the server.
            connectRetryAttempts = 5 // Maximum number of attempts for retrying a connection.
        }
    }
}
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
val client = HttpClient(Jetty) {
    engine {
        sslContextFactory = SslContextFactory()
    }
}
```

Artifact `io.ktor:ktor-client-jetty:$ktor_version`. \\
Transitive dependency: `org.eclipse.jetty.http2:http2-client:9.4.8.v20171121`.
{: .note.artifact }
