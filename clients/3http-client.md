---
title: Http Client
category: clients
permalink: /clients/http-client.html
children: /clients/http-client/
caption: Http Client 
---

{::options toc_levels="1..2" /}

In addition to HTTP serving, Ktor also includes a flexible asynchronous HTTP client.
This client supports several [configurable engines](/clients/http-client/engines.html), and has its own set of [features](#features).

The main functionality is available through the `io.ktor:ktor-client-core:$ktor_version` artifact.
And each engine, is provided in [separate artifacts](/clients/http-client/engines.html).
{: .note.artifact }

**Table of contents:**

* TOC
{:toc}

## Calls: Requests and Responses
{: #requests-responses }

You can check [how to make requests](/clients/http-client/calls/requests.html),
and [how to receive responses](/clients/http-client/calls/responses.html) in their respective sections.

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
{: #examples }

For more information, check the [examples page](/clients/http-client/examples.html) with some examples.

## Features
{: #features}

For more information, check the [features page](/clients/http-client/features.html) with all the available features.
