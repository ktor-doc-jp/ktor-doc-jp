---
title: Migration
caption: Migration
category: quickstart
permalink: /quickstart/migration.html
priority: 99
---

## Migrating from 0.9.1 to 0.9.2
{: #0.9.2}

Ktor 0.9.2 fixes some bugs, improves the overall performance of the server, partially starts supporting JVM 9,
and introduces new APIs and changes for some of them.

In this section, we will discuss how to convert existing code from 0.9.1 to 0.9.2.

For a changelog including fixes, improvements, and new APIs, check the [changelog](/quickstart/changelog.html#0.9.2) instead.

### Authentication

The biggest change in this version since 0.9.1, is authentication. Which has been redesigned:

In previous versions, you had to define an `authentication` block inside your Application or in a Route block,
applying that authentication to all the subroutes matching that block.

That forced you to redefine several authentication providers, or in some cases that would force you to include
authentication in unexpected routes.

```kotlin
authentication {
    basicAuthentication("ktor") { credentials ->
        if (credentials.password == "${credentials.name}123") UserIdPrincipal(credentials.name) else null
    }
}
```

In 0.9.2, all the authentication mechanisms are defined at the Application level and have an optional name
associated with them.
Also, the method names for defining different mechanisms have changed. Now the new name of the authentication mechanism
is part of the function call, and all its old parameters are defined using a DSL instead.

For example, to define a basic authentication `myauth1`, you should add this code to your application configuration: 

```kotlin
install(Authentication) {
    basic(name = "myauth1") {
        realm = "Ktor Server"
        validate { credentials ->
            if (credentials.password == "${credentials.name}123") UserIdPrincipal(credentials.name) else null
        }
    }
}
```

And now you can create a Route node to apply the defined authentication to several routes using the `authenticate` method:

```kotlin
routing {
    authenticate("myauth1") {
        get("/authenticated/route1") {
            // ...
        }    
        get("/other/route2") {
            // ...
        }    
    }
    get("/") {
        // ...
    }
}
```

*Authentication method name changes:*

* `basicAuthentication` → `basic`
* `formAuthentication` → `form`
* `digestAuthentication` → `digest`
* `jwtAuthentication` → `jwt`
* `oauthAuthentication` → `oauth`

You can read the new [authentication page](/features/authentication.html) to see in detail how to use the new methods.

### Test host reworked, handleRequest reads the body and redirects the exceptions correctly

The `body` property from `TestApplicationRequest` builder has been changed to a suspend `setBody` method: 

```kotlin
handleRequest(HttpMethod.Post, "/") {
    addHeader("Accept", "text/plain")
    addHeader("Content-Type", "application/json")
    //body = """{"id":1,"title":"Hello, World!"}"""
    setBody("""{"id":1,"title":"Hello, World!"}""")
}.response.let { response ->
    // ...
}
```

Before 0.9.2, you had to call the method `awaitCompletion` in the response in the case the request
was not generating the response body synchronously. In 0.9.2, the `awaitCompletion` method doesn't exist,
and it awaits completion automatically before returning the response.

### IncomingContent deprecation

If you are using `call.request.receiveContent().readChannel()`, `call.request.receiveContent().multiPartData()`
or `call.request.receiveContent().inputStream()`, you should consider changing it to
`call.receive<ByteReadChannel>()`, `call.receive<MultiPartData>()` and/or `call.receive<InputStream>()`
since it is deprecated and will be removed in future versions of Ktor.

Also, remember that `InputStream` is a synchronous API, so you should avoid it if possible.

### WebSockets

In order to support WebSockets at the client side, we have changed some of the transitive dependencies
and moved some classes: now there is a transitive dependency called `ktor-http-cio` that includes common
WebSockets code among other things, which the `ktor-websockets` server feature depends on.
But since it is a transitive dependency, that should be transparent to you.

Classes like `WebSocketSession` and `Frame` have been moved from the `io.ktor.websocket` package to the
`io.ktor.http.cio.websocket` package.

```
import io.ktor.websocket.*
```
→
```
import io.ktor.http.cio.websocket.*
```

### HttpClient

When building a new CIO HttpClient, while configuring the endpoint
(it has been renamed from `endpointConfig` to `endpoint`, and now the property is immutable,
so you have to mutate its contents):

*Prior to 0.9.2:*
```kotlin
val client = HttpClient(CIO.config { 
    endpointConfig = EndpointConfig().apply {    
    }
})
```

*After 0.9.2:*
```kotlin
val client = HttpClient(CIO.config { 
    endpoint.apply {
        // ...    
    }
})
```
