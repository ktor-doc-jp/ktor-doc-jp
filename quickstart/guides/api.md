---
title: REST API
caption: How to create an API using ktor
category: quickstart
---

{::options toc_levels="1..2" /}

In this guide you will learn how to create an API using ktor.
We are going to create a simple API to store simple text snippets (like a small pastebin-like API).

To achieve this, we are going to use the [Routing], [StatusPages], [Authentication], [JWT Authentication],
[CORS], [ContentNegotiation] and [Jackson] features.

[Routing]: /features/routing.html
[StatusPages]: /features/status-pages.html
[Authentication]: /features/authentication.html
[JWT Authentication]: /features/authentication/jwt.html
[CORS]: /features/cors.html
[ContentNegotiation]: /features/content-negotiation.html
[Jackson]: /features/content-negotiation/jackson.html

**Table of contents:**

* TOC
{:toc}

## Setting up the project

The first step is to set up a project. You can follow the [Quick Start](/quickstart/index.html) guide, or use the following form to create one:

[**Open the pre-configured generator form**](javascript:$('#start_ktor_io_form').toggle())

<iframe src="{{ site.ktor_init_tools_url }}#dependency=auth&dependency=auth-jwt&dependency=ktor-jackson&dependency=cors&artifact-group=com.example&artifact-name=api-example" id="start_ktor_io_form" style="border:1px solid #343a40;width:100%;height:500px;display:none;"></iframe>

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

![](/quickstart/guides/api/snippets_get.png){:.rounded-shadow}

## Handling other HTTP methods

REST APIs use most of the HTTP methods/verbs (_HEAD_, _GET_, _POST_, _PUT_, _PATCH_, _DELETE_, _OPTIONS_) to perform operations.
Let's create a route to add new snippets. For this, we will need to read the JSON body of the POST request.
For this we will use `call.receive<Type>()`:

```kotlin
data class PostSnippet(val snippet: PostSnippet.Text) {
    data class Text(val text: String)
}

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

You can use postman or curl to perform a POST call easily:

Postman:

![](/quickstart/guides/api/postman.png)`{:.rounded-shadow}`

CURL:

<table class="compare-table"><thead><tr><th>Bash:</th><th>Response:</th></tr></thead><tbody><tr><td markdown="1">

```bash
curl \
  --request POST \
  --header "Content-Type: application/json" \
  --data '{"snippet" : {"text" : "mysnippet"}}' \
  http://127.0.0.1:8080/snippets
```

</td><td markdown="1">

```json
{
  "OK" : true
}
```

</td></tr></tbody></table>

Let's do the GET request again:

![](/quickstart/guides/api/snippets_get_new.png){:.rounded-shadow}

Nice!

## Grouping routes together

Now we have two separate routes that share the path (but not the method) and we don't want to repeat ourselves.

We can group routes with the same prefix, using the `route(path) { }` block. For each HTTP method, there is an
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

We will need to install and configure JWT (replacing the basic auth):

```kotlin
open class SimpleJWT(val secret: String) {
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier = JWT.require(algorithm).build()
    fun sign(name: String): String = JWT.create().withClaim("name", name).sign(algorithm)
}

fun Application.module() {
    val jwt = SimpleJWT("my-super-secret-for-jwt")
    install(Authentication) {
        jwt {
            verifier(jwt.verifier)
            validate {
                UserIdPrincipal(it.payload.getClaim("name").asString())
            }
        }
    }
    // ...
}
```

We will also need a data source holding usernames and passwords. One simple option would be:

```kotlin
class User(val name: String, val password: String)

val users = Collections.synchronizedMap(
    listOf(User("test", "test"))
        .associateBy { it.name }
        .toMutableMap()
)
```

With all this, we can already create a route for logging or registering users:

```kotlin
routing {
    post("/login-register") {
        val post = call.receive<LoginRegister>()
        val user = users.getOrPut(post.user) { User(post.user, post.password) }
        if (user.password != post.password) error("Invalid credentials")
        call.respond(mapOf("token" to jwt.sign(user.name)))
    }
}
```

With all this, we can already try to obtain a JWT token for our user:

<table class="compare-table"><thead><tr><th>Bash:</th><th>Response:</th></tr></thead><tbody><tr><td markdown="1">

```bash
curl -v \
  --request POST \
  --header "Content-Type: application/json" \
  --data '{"user" : "test", "password" : "test"}' \
  http://127.0.0.1:8080/login-register
```

</td><td markdown="1">

```json
{
  "token" : "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoidGVzdCJ9.96At6bwFhxebk4xk4tpkOFj-3ThxkLFNHkHaKoedOfA"
}
```

</td></tr></tbody></table>

And with that token, we can already publish snippets:

<table class="compare-table"><thead><tr><th>Bash:</th><th>Response:</th></tr></thead><tbody><tr><td markdown="1">

```bash
curl -v \
  --request POST \
  --header "Content-Type: application/json" \
  --header "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoidGVzdCJ9.96At6bwFhxebk4xk4tpkOFj-3ThxkLFNHkHaKoedOfA" \
  --data '{"snippet" : {"text": "hello-world-jwt"}}' \
  http://127.0.0.1:8080/snippets
```

</td><td markdown="1">

```json
{
  "OK" : true
}
```

</td></tr></tbody></table>

## Associating user to snippets

Since we are posting snippets with an authenticated route, we have access to the generated `Principal` that includes
the username. So we should be able to access that user and associate it to the snippet.

First of all, we will need to associate user information to snippets:

```kotlin
data class Snippet(val user: String, val text: String)

val snippets = Collections.synchronizedList(mutableListOf(
    Snippet(user = "test", text = "hello"),
    Snippet(user = "test", text = "world")
))
```

Now we can use the principal information (that is generated by the authentication feature when authenticating JWT)
when inserting new snippets:

```kotlin
routing {
    // ...
    route("/snippets") {
        // ...
        authenticate {
            post {
                val post = call.receive<PostSnippet>()
                val principal = call.principal<UserIdPrincipal>() ?: error("No principal")
                snippets += Snippet(principal.name, post.snippet.text)
                call.respond(mapOf("OK" to true))
            }
        }
    }
}
```

Let's try this:

<table class="compare-table"><thead><tr><th>Bash:</th><th>Response:</th></tr></thead><tbody><tr><td markdown="1">

```bash
curl -v \
  --request GET \
  http://127.0.0.1:8080/snippets
```

</td><td markdown="1">

```json
{
  "snippets" : [ {
    "user" : "test",
    "text" : "hello"
  }, {
    "user" : "test",
    "text" : "world"
  }, {
    "user" : "test",
    "text" : "hello-world-jwt"
  } ]
}
```

</td></tr></tbody></table>

Awesome!

## StatusPages

Now let's refine things a bit. A REST API should use Http Status codes to provide semantic information about errors.
Right now, when an exception is thrown (for example when trying to get a JWT token from an user that already exists,
but with a wrong password), a 500 server error is returned. We can do it better, and the StatusPages features
will allow you to do this by capturing specific exceptions and generating the result.

Let's create a new exception type:

```kotlin
class InvalidCredentialsException(message: String) : RuntimeException(message)
```

Now, let's install the StatusPages feature, register this exception type, and generate an Unauthorized page: 

```kotlin
fun Application.module() {
    install(StatusPages) {
        exception<InvalidCredentialsException> { exception ->
            call.respond(HttpStatusCode.Unauthorized, mapOf("OK" to false, "error" to (exception.message ?: "")))
        }
    }
    // ...
}
```

We should also update our login-register page to throw this exception:

```kotlin
routing {
    post("/login-register") {
        val post = call.receive<LoginRegister>()
        val user = users.getOrPut(post.user) { User(post.user, post.password) }
        if (user.password != post.password) throw InvalidCredentialsException("Invalid credentials")
        call.respond(mapOf("token" to jwt.sign(user.name)))
    }
}
```

Let's try this:

<table class="compare-table"><thead><tr><th>Bash:</th><th>Response:</th></tr></thead><tbody><tr><td markdown="1">

```bash
curl -v \
  --request POST \
  --header "Content-Type: application/json" \
  --data '{"user" : "test", "password" : "invalid-password"}' \
  http://127.0.0.1:8080/login-register
```

</td><td markdown="1">

```bash
< HTTP/1.1 401 Unauthorized
< Content-Length: 53
< Content-Type: application/json; charset=UTF-8
```
```json
{
  "OK" : false,
  "error" : "Invalid credentials"
}
```

</td></tr></tbody></table>

Things are getting better!

## CORS

Now suppose we need this API to be accessible via JavaScript from another domain. We will need to configure CORS.
And Ktor has a feature to configure this:

```kotlin
fun Application.module() {
    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        allowCredentials = true
        anyHost()
    }
    // ...
}
```

Now our API is accessible from any host :)

## Exercises

After following this guide, as an exercise, you can try to do the following exercises:

### Exercise 1

Add unique ids to each snippet and add a DELETE http verb to /snippets allowing an authenticated user to delete
her snippets.  

### Exercise 2

Store users and snippets in a database. 
