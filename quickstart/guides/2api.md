---
title: HTTP API
caption: "ガイド: Ktorを用いたAPIの作り方"
category: quickstart
permalink: /quickstart/guides/api.html
ktor_version_review: 1.0.0
---

{::options toc_levels="1..2" /}

本ページでは、Ktorを用いたAPIの作り方を学びます。
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
[Quick Start](/quickstart/index.html) のページに従って作成するか、下記の Ktor Project Generator を使ってプロジェクトを作成してください。

{% include preconfigured-form.html hash="dependency=auth&dependency=auth-jwt&dependency=ktor-jackson&dependency=cors&artifact-group=com.example&artifact-name=api-example" %}

## シンプルなルーティング

まずはじめに、 [Routing Feature](/servers/features/routing.html) を使っていきます。
Ktor では様々な機能を Feature という形で提供し、その Feature をインストールすることで利用可能になりますが、
Routing Feature は Ktor のコア機能の一つなので、新たに Feature を追加する必要はありません。

Routing Feature の DSL ブロックのひとつである `routing { }` ブロックを用いることで、自動的にインストールされます。

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
[Content Negotiation](/servers/features/content-negotiation.html) に [Jackson](/servers/features/content-negotiation/jackson.html) をインストールすると、 JSON を返却できるようになります。

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

ブラウザや HTTP クライアントから `http://127.0.0.1:8080/snipets` へアクセスすると、 `{"OK": true}` が返却されるはずです。

`Response pipeline couldn't transform '...' to the OutgoingContent` のようなエラーが返却された場合は、
[ContentNegotiation](/servers/features/content-negotiation.html) に Jackson がインストールされているか確認しましょう。
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
これを実現するためには、 POST リクエストで送信される JSON 形式のリクエストボディを読み取る必要があります。
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

### IntelliJ IDEA Ultimate の場合
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

複数の HTTP リクエストの定義を plain text ファイルや scratch ファイルに定義することができ、
ヘッダを指定したり、インラインでペイロードを指定したり、 JSON ファイルに定義した環境変数を使用したり、
JavaScript でレスポンスを処理してアサーションしたり、認証情報を環境変数に保存した上で別のリクエストで利用したりできます。
また、自動補完やテンプレート、 Content-Type (JSON, XML など) に応じた
自動[言語インジェクション](https://pleiades.io/help/idea/using-language-injections.html)にも対応しています。
{: .note}

エディタ上で簡単にバックエンドのテストができるだけでなく、エンドポイントをファイルに記述しておくことで、
APIのドキュメント化にも役立ちます。
また、レスポンスをローカルに保存することで差分を視覚化できます。
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

もう一度 GET リクエストを投げてみましょう。

![](/quickstart/guides/api/snippets_get_new.png){:.rounded-shadow}

いいね!

## ルーティングのグループ化

同じパスで HTTP メソッドだけが異なる場合、ルーティングの定義を重複して定義したくないですよね。

prefix が同じルーティングは、 `route(path) { }` ブロックを用いることでグループ化できます。
複数の HTTP メソッドに対し、ルーティングのリーフノードを共有するかのごとく、同一のパスをオーバーロードできます。

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

## 認証

誰からでもスニペットを投稿できるのは避けたいですよね。
ユーザ名とパスワードを用いた HTTP の Basic 認証でこれを制限してみましょう。
Basic 認証をするために、 Authentication Feature をインストールします。

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

Authentication Feature をインストールして設定ができたら、認証を要求したいルーティング群を `authenticat { }` ブロック内に入れましょう。

今回は、GET リクエストでは認証不要で、 POST リクエストでは Basic 認証を要求するようにしてみました。

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

## JWT 認証

固定的な認証 (Basic 認証) を用いる代わりに、 JWT 認証を用いてみましょう。

`login-register` ルーティングを追加していきます。
このルーティングは、ユーザ情報が存在しない場合は登録を行い、認証に成功した場合や登録が成功した場合は JWT トークンを返却します。
JWT トークンはユーザ名と、ユーザがスニペットを投稿するためのリンクを持ちます。

まずは、 Authentication のインストールと JWT の設定を行います。
(Basic 認証と置き換えしました。)

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

ユーザ名とパスワードを保持するためのデータソースも必要です。
単純な例を下記に示します。

```kotlin
class User(val name: String, val password: String)

val users = Collections.synchronizedMap(
    listOf(User("test", "test"))
        .associateBy { it.name }
        .toMutableMap()
)
class LoginRegister(val user: String, val password: String)

```

以上で、ログインまたはユーザ登録ができるルーティング (`/login-register`) を作成できるようになりました。

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

これでユーザは JWT トークンを取得できるようになりました。

{% comment %}
### IntelliJ
{% endcomment %}

IntelliJ IDEA Ultimate の HTTP クライアントを用いると、 POST リクエストを作成し、
レスポンス内容が期待通りか確認し、環境変数にトークンを保持することができます。

![](/quickstart/guides/api/IU-http-login-register-request.png)

![](/quickstart/guides/api/IU-http-login-register-response.png)

環境変数 `{% raw %}{{auth_token}}{% endraw %}` を用いてリクエストを発行してみましょう。

![](/quickstart/guides/api/IU-http-snippets-env-auth_token-request.png)

![](/quickstart/guides/api/IU-http-snippets-env-auth_token-response.png)

localhost に加えて他のエンドポイントに対し簡単にテストをしたい場合は、 `http-client.env.json` ファイルを作成し、
下記のように環境変数の map を作成します。

![](/quickstart/guides/api/IU-env/http-client.env.json.png)

これでユーザ定義環境変数 `{% raw %}{{host}}{% endraw %}` を用いることができるようになります。
![](/quickstart/guides/api/IU-env/use_host_env.png)

リクエストを発行する際に、環境の選択ができるようになります。
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

このトークンを使用することで、スニペットの投稿ができます。

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

## ユーザとスニペットの関連付け

認証済のルーティングでスニペットを投稿しているため、ユーザ名などのユーザ情報を持っている `Principal` を参照できます。
この `Principal` を用いることで、ユーザとスニペットを関連付けることができます。

まずはじめに、ユーザ情報とスニペットを関連付けます。

```kotlin
data class Snippet(val user: String, val text: String)

val snippets = Collections.synchronizedList(mutableListOf(
    Snippet(user = "test", text = "hello"),
    Snippet(user = "test", text = "world")
))
```

これで、新規でスニペットを作成する際に、 Principal の情報 (JWT で認証した際に Authentication Feature が自動的に生成) も
付与できるようになりました。

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

実際に使ってみましよう!

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


素晴らしい!

## ステータスページ

ちょっと改良してみましょう。
HTTP API は HTTP ステータスコードを用いて、エラーに関するセマンティックな情報を提供する必要があります。
現状ではJWT トークンを取得する際に、すでに存在するユーザ名で登録しようとしたりユーザがパスワードを間違えた場合は例外が送出され、
500 サーバエラーが返却されます。
StatusPage Feature を利用することで、特定の例外をキャッチして何らかの結果を返却するなど、より適切なエラー処理を行えるようになります。

新しく例外型を作成してみましょう。

```kotlin
class InvalidCredentialsException(message: String) : RuntimeException(message)
```

次に、 StatusPages Feature をインストールし、先程作成した例外型を登録し、 Unauthorized ページを生成しましょう。

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

最後に、 `login-register` ページにて、認証に失敗した際に先程の例外を送出するようにします。

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

実際に使ってみましよう!

![](/quickstart/guides/api/IU-bad-credentials/bad-credentials-request.png)

![](/quickstart/guides/api/IU-bad-credentials/bad-credentials-response.png)

エラーがわかりやすくなりましたね!

## CORS

別のドメインから JavaScript を介してこのスニペット API を利用できるようにする必要があるとします。
そのためには、 CORS の設定が必要です。
Ktor では下記のように設定することができます。

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

これでどんなホストからもこの API を利用可能になりました :)

## コード全体


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

## 発展課題

時間がある方や、今回身につけた新しいスキルを練習してみたい方向けに、発展課題を用意しました。
是非挑戦してみてください!

### 発展課題 1

各スニペットに一意の ID を付与し、 `/snipetts` に HTTP DELETE メソッドを実装し、
認証済みユーザが自分のスニペットを削除できるようにしてみましょう。

### 発展課題 2

ユーザとスニペットをデータベースに保存してみましょう。
