---
title: Engines
category: clients
permalink: /clients/http-client/engines.html
caption: HTTP Client Engines 
---

Ktor HTTP Client has a common interface for performing HTTP Requests,
but allows to specify an engine that does the internal job.
Different engines has different configurations, dependencies an supporting features.

**Table of contents:**

* TOC
{:toc}

## Default engine
{: #default}

By calling to the `HttpClient` method without specifying an engine, it uses a default engine.

```kotlin
val client = HttpClient()
```

In the case of the JVM, the default engine is resolved with a ServiceLoader, getting the first one available.
Thus depends on the artifacts you have included.

For native, it uses the predefined one.

## Configuring engines
{: #configuring}

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

## JVM

### Apache
{: #apache}

Apache is the most configurable HTTP client about right now. It supports HTTP/1.1 and HTTP/2.
It is the only one that supports following redirects and allows you to configure timeouts,
proxies among other things it is supported by `org.apache.httpcomponents:httpasyncclient`.

A sample configuration would look like:

```kotlin
val client = HttpClient(Apache) {
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


{% include artifact.html kind="engine" class="io.ktor.client.engine.apache.Apache" artifact="io.ktor:ktor-client-apache" transitive="org.apache.httpcomponents:httpasyncclient" %}

### CIO
{: #cio}

CIO (Coroutine-based I/O) is a Ktor implementation with no additional dependencies and is fully asynchronous.
It only supports HTTP/1.x for now.

CIO provides `maxConnectionsCount` and a `endpointConfig` for configuring.

A sample configuration would look like:

```kotlin
val client = HttpClient(CIO) {
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

{% include artifact.html kind="engine" class="io.ktor.client.engine.cio.CIO" artifact="io.ktor:ktor-client-cio:$ktor_version" %}

### Jetty
{: #jetty}

Jetty provides an additional `sslContextFactory` for configuring. It only supports HTTP/2 for now.

A sample configuration would look like:

```kotlin
val client = HttpClient(Jetty) {
    engine {
        sslContextFactory = SslContextFactory()
    }
}
```

{% include artifact.html kind="engine" class="io.ktor.client.engine.jetty.Jetty" artifact="io.ktor:ktor-client-jetty:$ktor_version" transitive="org.eclipse.jetty.http2:http2-client" %}

## JVM and Android

### OkHttp
{: #okhttp }

Since Ktor 0.9.4, there is a engine based on OkHttp.

```kotlin
val client = HttpClient(OkHttp) {
    engine {
        // https://square.github.io/okhttp/3.x/okhttp/okhttp3/OkHttpClient.Builder.html
        config { // this: OkHttpClient.Builder ->
            // ...
            followRedirects(true)
            // ...
        }    
        
        // https://square.github.io/okhttp/3.x/okhttp/okhttp3/Interceptor.html
        addInterceptor(interceptor)
        addNetworkInterceptor(interceptor)

    }
    
}
```

{% include artifact.html kind="engine" class="io.ktor.client.engine.okhttp.OkHttp" artifact="io.ktor:ktor-client-okhttp:$ktor_version" transitive="com.squareup.okhttp3:okhttp" %}

## Android
{: #android }

The Android engine, doesn't have additional dependencies, and uses a ThreadPool with a normal HttpURLConnection,
to perform the requests. And can be configured like this:

```kotlin
val client = HttpClient(Android) {
    engine {
        connectTimeout = 100_000
        socketTimeout = 100_000
    }
}
```

{% include artifact.html kind="engine" class="io.ktor.client.engine.android.Android" artifact="io.ktor:ktor-client-android:$ktor_version" %}

## iOS
{: #ios }

The iOS engine, uses the asynchronous `NSURLSession` internally. And have no additional configuration.

```kotlin
val client = HttpClient(Ios) {
}
```

{% include artifact.html kind="engine" class="io.ktor.client.engine.ios.Ios" artifact="io.ktor:ktor-client-ios:$ktor_version" %}

## Testing
{: #testing }

### MockEngine

There is a engine specific for testing described in its own page: [MockEngine for testing](/clients/http-client/testing.html).
