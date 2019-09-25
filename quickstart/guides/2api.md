---
title: HTTP API
caption: "Guides: ktor を用いた API の作り方"
category: quickstart
permalink: /quickstart/guides/api.html
ktor_version_review: 1.0.0
---

{::options toc_levels="1..2" /}

本ページでは、 ktor を用いた API の作り方を学びます。
We are going to create a simple API to store simple text snippets (like a small pastebin-like API).
簡易版 pastebin API のような、単純なテキストスニペットを保存する API を作っていきます。

これから [Routing], [StatusPages], [Authentication], [JWT Authentication], [CORS], [ContentNegotiation], [Jackson] について学んでいきます。

[Routing]: /servers/features/routing.html
[StatusPages]: /servers/features/status-pages.html
[Authentication]: /servers/features/authentication.html
[JWT Authentication]: /servers/features/authentication/jwt.html
[CORS]: /servers/features/cors.html
[ContentNegotiation]: /servers/features/content-negotiation.html
[Jackson]: /servers/features/content-negotiation/jackson.html

多くの Web フレームワークにて REST API の作り方を説明していますが、実際には REST API ではなく HTTP API について説明しているものがほとんどです。
Ktor は、多くの他のフレームワークと同様に、 REST の設計原則に則ったシステムを作ることができます。
ですが、このチュートリアルでは、REST API についてではなく、 HTTP リクエストメソッドを用いて JSON や XML などのフォーマットで返却する API について説明します。
RESTful なシステムについてより詳しく知りたい場合は、 <https://ja.wikipedia.org/wiki/Representational_State_Transfer>{:target="_blank"} を参照してください。
{: .note }

**目次:**

* 目次
{:toc}

## プロジェクトの作成

まずはじめに、プロジェクトのセットアップから行います。
[Quick Start](/quickstart/index.html) のページに従うか、下の Ktor Project Generator を使ってプロジェクトを作成してください。

{% include preconfigured-form.html hash="dependency=auth&dependency=auth-jwt&dependency=ktor-jackson&dependency=cors&artifact-group=com.example&artifact-name=api-example" %}

## シンプルなルーティング

まずはじめに、 [Routing Feature](/servers/features/routing.html) を使っていきます。
Ktor では様々な機能を Feature という形で提供し、その Feature を install することで利用可能になります。
ですが、 Routing Feature は Ktor のコア機能の一つなので、新たに Feature を追加する必要はありません。

Routing Feature は DSL ブロックのひとつの `routing { }` を用いることで、自動的に install されます。

`routing` ブロックとその内部で利用できる `get` メソッドを用いて、 `OK` を返却するシンプルな GET API を作成してみましょう。

```kotlin
fun Application.module() {
    routing {
        get("/snippets") {
            call.respondText("OK")
        }
    }
}
```

## JSON の返却

HTTP API は JSON を返却することができます。
[Content Negotiation](/servers/features/content-negotiation.html) に [Jackson](/servers/features/content-negotiation/jackson.html) を install すると、 JSON を返却できるようになります。

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

JSON を返却するためには、 `call.respond` メソッドに任意のオブジェクトを渡す必要があります。

```kotlin
routing {
    get("/snippets") {
        call.respond(mapOf("OK" to true))
    }
}
```

ブラウザや HTTP クライアントから `http://localhost:8080/snipets` へアクセスすると、 `{"OK": true}` が返却されるはずです。

`Response pipeline couldn't transform '...' to the OutgoingContent` のようなエラーが返却された場合は、
[ContentNegotiation](/servers/features/content-negotiation.html) に Jackson が install されているか確認しましょう。
{: .note}

レスポンスオブジェクトの一部として任意の型のオブジェクトを渡すこともできます。
例えば下記のような場合は、

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

下記のように返却されます。

![](/quickstart/guides/api/snippets_get.png){:.rounded-shadow}

## HTTP メソッドの扱い方

HTTP API は様々な HTTP メソッド (_HEAD_, _GET_, _POST_, _PUT_, _PATCH_, _DELETE_, _OPTIONS_) を用いて操作を実行します。
新規でスニペットを追加する API を作成してみましょう。
このためには、 POST リクエストで送信される JSON 形式のリクエストボディを読み取る必要があります。
リクエストボディを読み取るためには、 `call.receive<Type()` を用います。

```kotlin
data class PostSnippet(val snippet: PostSnippet.Text) {
    data class Text(val text: String)
}

// ...

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

さっそく使ってみましょう。

IntelliJ IDEA Ultimate には強力な HTTP クライアントが同梱されています。
もし IntelliJ IDEA Ultimate を利用していない場合は、 postman や curl で代用可能です。

### IntelliJ IDEA Ultimate:
{: #first-request-intellij }

IntelliJ IDEA Ultimate や PhpStorm を始め、 JetBrains 製の IDE には
[Editor-Based Rest Client](https://blog.jetbrains.com/phpstorm/2017/09/editor-based-rest-client/){:target="_blank"}
が同梱されています。

まずは、 HTTP Request ファイル (拡張子 `api` または `http`) を作成します。
![](/quickstart/guides/api/IU-http-new-file.png)

次に、 HTTP メソッド、 URL 、ヘッダ 、 payload を記述します。

![](/quickstart/guides/api/IU-http-request.png)

```
POST http://127.0.0.1:8080/snippets
Content-Type: application/json

{"snippet": {"text" : "mysnippet"}}
```

最後に、 URL の横にある実行ボタンを押すことでリクエストが実行され、結果が表示されます。

![](/quickstart/guides/api/IU-http-response.png)

以上!

This allows you to define files (plain or scratches) that include definition for several HTTP requests,
allowing to include headers, provide a payload inline, or from files, use environment variables defined in a JSON file,
process the response using JavaScript to perform assertions, or to store some environment variables like
authentication credentials so they are available to other requests. It supports autocompletion, templates, and
automatic language injection based on Content-Type, including JSON, XML, etc..
{: .note}

In addition to easily test your backends inside your editor, it also helps your to document your APIs
by including a file with the endpoints on it.
And allows you fetch and locally store responses and visually compare them.
{: .note}

### CURL:
{: #first-request-curl }

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

---

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
    val simpleJwt = SimpleJWT("my-super-secret-for-jwt")
    install(Authentication) {
        jwt {
            verifier(simpleJwt.verifier)
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
class LoginRegister(val user: String, val password: String)

```

With all this, we can already create a route for logging or registering users:

```kotlin
routing {
    post("/login-register") {
        val post = call.receive<LoginRegister>()
        val user = users.getOrPut(post.user) { User(post.user, post.password) }
        if (user.password != post.password) error("Invalid credentials")
        call.respond(mapOf("token" to simpleJwt.sign(user.name)))
    }
}
```

Now we can already try to obtain a JWT token for our user:

{% comment %}
### IntelliJ
{% endcomment %}

Using the Editor-Based HTTP client for IntelliJ IDEA Ultimate,
you can make the POST request, and check that the content is valid,
and store the token in an environment variable:

![](/quickstart/guides/api/IU-http-login-register-request.png)

![](/quickstart/guides/api/IU-http-login-register-response.png)

Now you can make a request using the environment variable `{% raw %}{{auth_token}}{% endraw %}`:

![](/quickstart/guides/api/IU-http-snippets-env-auth_token-request.png)

![](/quickstart/guides/api/IU-http-snippets-env-auth_token-response.png)

If you want to easily test different endpoints in addition to localhost,
you can create a `http-client.env.json` file and put a map with environments
and variables like this:

![](/quickstart/guides/api/IU-env/http-client.env.json.png)

After this, you can start using the user-defined `{% raw %}{{host}}{% endraw %}` env variable:
![](/quickstart/guides/api/IU-env/use_host_env.png)

When trying to run a request, you will be able to choose the environment to use:
![](/quickstart/guides/api/IU-env/select_env_for_running.png)

{% comment %}
### Curl

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

{% endcomment %}

## Associating users to snippets

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

![](/quickstart/guides/api/IU-final/final-request.png)

![](/quickstart/guides/api/IU-final/final-response.png)

{% comment %}
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
{% endcomment %}


Awesome!

## StatusPages

Now let's refine things a bit. A HTTP API should use HTTP Status codes to provide semantic information about errors.
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
        call.respond(mapOf("token" to simpleJwt.sign(user.name)))
    }
}
```

Let's try this:

![](/quickstart/guides/api/IU-bad-credentials/bad-credentials-request.png)

![](/quickstart/guides/api/IU-bad-credentials/bad-credentials-response.png)


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

## Full Source


{% capture application-kt %}
```kotlin
package com.example

import com.auth0.jwt.*
import com.auth0.jwt.algorithms.*
import com.fasterxml.jackson.databind.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.util.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val simpleJwt = SimpleJWT("my-super-secret-for-jwt")
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
    install(StatusPages) {
        exception<InvalidCredentialsException> { exception ->
            call.respond(HttpStatusCode.Unauthorized, mapOf("OK" to false, "error" to (exception.message ?: "")))
        }
    }
    install(Authentication) {
        jwt {
            verifier(simpleJwt.verifier)
            validate {
                UserIdPrincipal(it.payload.getClaim("name").asString())
            }
        }
    }
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT) // Pretty Prints the JSON
        }
    }
    routing {
        post("/login-register") {
            val post = call.receive<LoginRegister>()
            val user = users.getOrPut(post.user) { User(post.user, post.password) }
            if (user.password != post.password) throw InvalidCredentialsException("Invalid credentials")
            call.respond(mapOf("token" to simpleJwt.sign(user.name)))
        }
        route("/snippets") {
            get {
                call.respond(mapOf("snippets" to synchronized(snippets) { snippets.toList() }))
            }
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
}

data class PostSnippet(val snippet: PostSnippet.Text) {
    data class Text(val text: String)
}

data class Snippet(val user: String, val text: String)

val snippets = Collections.synchronizedList(mutableListOf(
    Snippet(user = "test", text = "hello"),
    Snippet(user = "test", text = "world")
))

open class SimpleJWT(val secret: String) {
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier = JWT.require(algorithm).build()
    fun sign(name: String): String = JWT.create().withClaim("name", name).sign(algorithm)
}

class User(val name: String, val password: String)

val users = Collections.synchronizedMap(
    listOf(User("test", "test"))
        .associateBy { it.name }
        .toMutableMap()
)

class InvalidCredentialsException(message: String) : RuntimeException(message)

class LoginRegister(val user: String, val password: String)
```
{% endcapture %}



{% capture my-api-http %}
```
{% raw %}
# Get all the snippets
GET {{host}}/snippets

###

# Register a new user
POST {{host}}/login-register
Content-Type: application/json

{"user" : "test", "password" : "test"}

> {%
client.assert(typeof response.body.token !== "undefined", "No token returned");
client.global.set("auth_token", response.body.token);
%}

###

# Put a new snippet (requires registering)
POST {{host}}/snippets
Content-Type: application/json
Authorization: Bearer {{auth_token}}

{"snippet" : {"text": "hello-world-jwt"}}

###

# Try a bad login-register
POST http://127.0.0.1:8080/login-register
Content-Type: application/json

{"user" : "test", "password" : "invalid-password"}

###
{% endraw %}
```
{% endcapture %}

{% capture http-client-env-json %}
```json
{
  "localhost": {
    "host": "http://127.0.0.1:8080"
  },
  "prod": {
    "host": "https://my.domain.com"
  }
}
```
{% endcapture %}

{% include tabbed-code.html
    tab1-title="application.kt" tab1-content=application-kt
    tab2-title="my-api.http" tab2-content=my-api-http
    tab3-title="http-client.env.json" tab3-content=http-client-env-json
%}

## Exercises

After following this guide, as an exercise, you can try to do the following exercises:

### Exercise 1

Add unique ids to each snippet and add a DELETE http verb to `/snippets` allowing an authenticated user to delete
her snippets.  

### Exercise 2

Store users and snippets in a database. 

