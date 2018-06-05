---
title: REST API
caption: How to create an API using ktor
category: quickstart
---

In this guide you will learn how to create an API using ktor.
We are going to create a simple API to store simple text snippets (like a small pastebin-like API).

To achieve this, we are going to use the
[Routing](/features/routing.html),
[StatusPages](/features/status-pages.html),
[Authentication](/features/authentication.html),
[CORS](/features/cors.html),
[ContentNegotiation](/features/content-negotiation.html) and
[Jackson](/features/content-negotiation/jackson.html)
features.

**Table of contents:**

* TOC
{:toc}

## Setting up the project

The first step is to set up a project. You can follow the [Quick Start](/quickstart/index.html) guide, or use the following form to create one:

[**Open the pre-configured generator form**](javascript:$('#start_ktor_io_form').toggle())

<iframe src="{{ site.start_ktor_io_url }}#dependency=auth&dependency=auth-jwt&dependency=ktor-jackson&artifact-group=com.example&artifact-name=api-example" id="start_ktor_io_form" style="border:1px solid #343a40;width:100%;height:500px;display:none;"></iframe>

## Simple routing

First of all, we are going to use the routing feature. This feature is part of the Ktor's core, so you won't need
to include any additional artifact.

This feature is installed automatically when using the `routing { }` block.

Let's start creating a simple GET route that responds with 'OK':

```kotlin
fun Application.module() {
    routing {
        get("/snippets") {
            call.respondText("OK")
        }
    }
}
```

## Serving JSON content

A REST API usually responds with JSON. You can use the *Content Negotiation* feature with *Jackson* for this:

```kotlin
fun Application.module() {
    install(ContentNegotiation) {
        jackson {
        }
    }
    routing {
        // ...
    }
}
```

To respond to a call with a JSON. You have to call the `call.respond` method with an arbitrary object.

```kotlin
routing {
    get("/snippets") {
        call.respond(mapOf("OK" to true))
    }
}
```

Now the browser should respond to `http://127.0.0.1:8080/snippets` with `{"OK":true}`

If you get an error like `Response pipeline couldn't transform '...' to the OutgoingContent`, check that you have
installed the ContentNegotiation feature with Jackson.
{: .note}

You can also use typed objects as part of the reply (but ensure that your classes are not defined
inside a function or it won't work). So for example:

```kotlin
data class Snippet(val text: String)

val snippets = Collections.synchronizedList(mutableListOf(
    Snippet("hello"),
    Snippet("world")
))

fun Application.module() {
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT) // Pretty Prints the JSON
        }
    }
    routing {
        get("/snippets") {
            call.respond(mapOf("snippets" to synchronized(snippets) { snippets.toList() }))
        }
    }
}
```

Would reply with:

![](/quickstart/guides/api/snippets_get.png){: style="box-shadow: 0px 0px 10px #999;"}

## Handling other verbs

REST APIs use most of the HTTP verbs (_GET_, _POST_, _PUT_, _PATCH_, _DELETE_) to perform operations.
Let's create a route to add new snippets. For this, we will need to read the JSON body of the POST request.
For this we will use `call.receive<Type>()`:

```kotlin
data class PostSnippet(val snippet: Snippet)

routing {
    get("/snippets") {
        call.respond(mapOf("snippets" to synchronized(snippets) { snippets.toList() }))
    }
    post("/snippets") {
        val post = call.receive<PostSnippet>()
        snippets += Snippet(post.snippet.text)
        call.respond(mapOf("OK" to true))
    }
}
```

You can use postman to perform a simulated call:

![](/quickstart/guides/api/postman.png)

Let's do the GET request again:

![](/quickstart/guides/api/snippets_get_new.png){: style="box-shadow: 0px 0px 10px #999;"}

Nice!

## Grouping routes together

Now we have two separate routes that share the path (but not the verb) and we don't want to repeat ourselves.

We can group routes with the same prefix, using the `route(path) { }` block. For each HTTP verb, there is an
overload without the route path argument that we can use at routing leaf nodes:

```kotlin
routing {
    route("/snippets") {
        get {
            call.respond(mapOf("snippets" to synchronized(snippets) { snippets.toList() }))
        }
        post {
            val post = call.receive<PostSnippet>()
            snippets += Snippet(post.snippet.text)
            call.respond(mapOf("OK" to true))
        }
    }
}
```

## Authentication

It would be a good idea to prevent everyone from posting snippets. For now, we are going to limit it using
http's basic authentication with a fixed user and password. To do it, we are going to use the authentication feature.

```kotlin
fun Application.module() {
    install(Authentication) {
        basic {
            realm = "myrealm" 
            validate { if (it.name == "user" && it.password == "password") UserIdPrincipal("user") else null }
        }
    }
    // ...
}
```

After installing and configuring the feature, we can group some routes together to be authenticated with the
`authenticate { }` block.

In our case, we are going to keep the get call unauthenticated, and going to require authentication for the post one:

```kotlin
routing {
    route("/snippets") {
        get {
            call.respond(mapOf("snippets" to synchronized(snippets) { snippets.toList() }))
        }
        authenticate {
            post {
                val post = call.receive<PostSnippet>()
                snippets += Snippet(post.snippet.text)
                call.respond(mapOf("OK" to true))
            }        
        }
    }
}
```

## JWT Authentication

Instead of using a fixed authentication, we are going to use JWT tokens.

We are going to add a login-register route. That route will register a user if it doesn't exist,
and for a valid login or register it will return a JWT token.
The JWT token will hold the user name, and posting will link a snippet to the user.

## StatusPages

## CORS
