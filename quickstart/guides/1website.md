---
title: Website
caption: "Guides: ktorを用いた簡単なWebサイトの作り方"
category: quickstart
permalink: /quickstart/guides/website.html
ktor_version_review: 1.0.0
---

{::options toc_levels="1..2" /}

本ページでは, Ktorを用いた簡単なWebサイトの作り方を学びます。ユーザー、ログインフォーム、及び永続的なセッションを維持するKtor上でHTMLがレンダリングされた簡単なWebサイトを作っていきます。

これから、[Routing]、[StatusPages]、[Authentication]、[Sessions]、[StaticContent]、[FreeMarker]、[HTML DSL]について学んでいきます。

[Routing]: /servers/features/routing.html
[StatusPages]: /servers/features/status-pages.html
[Authentication]: /servers/features/authentication.html
[Sessions]: /servers/features/sessions.html
[StaticContent]: /servers/features/static-content.html
[FreeMarker]: /servers/features/templates/freemarker.html
[HTML DSL]: /servers/features/templates/html-dsl.html

**Table of contents:**

* TOC
{:toc}

## プロジェクトの作成 

まずはじめにプロジェクトのセットアップから行います。[Quick Start](/quickstart/index.html) のページにしたがって作成するか、下記のKtor Project Generatorを使ってプロジェクトを作成してください。

{% include preconfigured-form.html hash="dependency=html-dsl&dependency=css-dsl&dependency=freemarker&dependency=static-content&dependency=auth&dependency=ktor-sessions&dependency=status-pages&dependency=routing&artifact-name=website-example" %}

## シンプルなルーティング 

まずはじめに、 [Routing Feature](/servers/features/routing.html) を使っていきます。
Ktor では様々な機能を Feature という形で提供し、その Feature をインストールすることで利用可能になりますが、
Routing Feature は Ktor のコア機能の一つなので、新たに Feature を追加する必要はありません。

Routing Feature の DSL ブロックのひとつである `routing { }` ブロックを用いることで、自動的にインストールされます。

`routing` ブロックとその内部で利用できる `get` メソッドを用いて、`OK`を返却するシンプルなGET APIを作成してみましょう:

```kotlin
fun Application.module() {
    routing {
        get("/") {
            call.respondText("OK")
        }
    }
}
```

## FreeMarkerを用いたHTMLの生成

Apache FreeMarkerはJVMのためのテンプレートエンジンです。したがってKotlinにおいても使うことができます。
Ktor においては最初からFeatureとしてサポートされています。

ここでは、 `templates`フォルダ以下にリソースの一部として埋め込まれたテンプレートを保存します。

新しく`resources/templates/index.ftl`ファイルを作成して, リストを表示する以下のHTMLを記述してください:

```freemarker
<#-- @ftlvariable name="data" type="com.example.IndexData" -->
<html>
	<body>
		<ul>
		<#list data.items as item>
			<li>${item}</li>
		</#list>
		</ul>
	</body>
</html>
```

IntelliJ IDEA Ultimate はFreeMarkerのオートコンプリートと変数のHintingをサポートしています。
{:.note}

次に、 FreeMarker Featureをインストールして、テンプレートに値を渡すroutingを作成しましょう:

```kotlin
data class IndexData(val items: List<Int>)

fun Application.module() {
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }
    
    routing {
        get("/html-freemarker") {
            call.respond(FreeMarkerContent("index.ftl", mapOf("data" to IndexData(listOf(1, 2, 3))), ""))
        }
    }
}
```

サーバーを起動して、<http://127.0.0.1:8080/html-freemarker>ページをブラウザで確認すれば、リストが表示されていることを確認できるでしょう:

![](/quickstart/guides/website/website1.png){:.rounded-shadow}

いいね!

## 静的ファイルの配信: styles, scripts, images...

In addition to templates, you will want to serve static content.
Static content will serve faster, and is compatible with other features like Partial Content that allows
you to resume downloads or partially download files.

For now, we are going to serve a simple `styles.css` file to apply styles to our simple page.

Serving static files doesn't require installing any features, but it is a plain Route handler.
To serve static files at the `/static` url, from `/resources/static`, you would write the following code:

```kotlin
routing {
    // ...
    static("/static") {
        resources("static")
    }
}
```

Now let's create the `resources/static/styles.css` file with the following content:

```css
body {
    background: #B9D8FF;
}
```

In addition to this, we will have to update our template to include the `style.css` file:
```freemarker
<#-- @ftlvariable name="data" type="com.example.IndexData" -->
<html>
    <head>
        <link rel="stylesheet" href="/static/styles.css">
    </head>
    <body>
	<!-- ... -->
    </body>
</html>
```

And the result:

![](/quickstart/guides/website/website2.png){:.rounded-shadow}

Now we have a colorful website from 1990!

Static files are not only text files! Try to add an image (what about a fancy animated blinking gif file? 👩🏻‍🎨) to the `static` folder, and include a `<img src="...">` tag to the HTML template.
{: .note.exercise}

## Enabling partial content: large files and videos

Though not really needed for this specific case, if you enable partial content support, people will be able
to resume larger static files on connections with frequent problems, or allow seeking support when
serving and watching videos.

Enabling partial content is straightforward:

```kotlin
install(PartialContent) {
}
```

## Creating a form

Now we are going to create a fake login form. To make it simple, we are going to accept users with the same password,
and we are not going to implement a registration form.

Create a `resources/templates/login.ftl`:

```kotlin
<html>
<head>
    <link rel="stylesheet" href="/static/styles.css">
</head>
<body>
<#if error??>
    <p style="color:red;">${error}</p>
</#if>
<form action="/login" method="post" enctype="application/x-www-form-urlencoded">
    <div>User:</div>
    <div><input type="text" name="username" /></div>
    <div>Password:</div>
    <div><input type="password" name="password" /></div>
    <div><input type="submit" value="Login" /></div>
</form>
</body>
</html>
```

In addition to the template, we need to add some logic to it. In this case we are going to handle GET and POST methods in different blocks of code:

```kotlin
route("/login") {
    get {
        call.respond(FreeMarkerContent("login.ftl", null))
    }
    post {
        val post = call.receiveParameters()
        if (post["username"] != null && post["username"] == post["password"]) {
            call.respondText("OK")
        } else {
            call.respond(FreeMarkerContent("login.ftl", mapOf("error" to "Invalid login")))
        }
    }
}
```

As we said, we are accepting `username` with the same `password`, but we are not accepting null values.
If the login is valid, we respond with a single OK for now, while we reuse the template if the login fails
to display the same form but with an error.

## Redirections

In some cases, like route refactoring or forms, we will want to perform redirections (either temporary or permanent).
In this case, we want to temporarily redirect to the homepage upon successful login, instead of replying with plain text.

<table class="compare-table"><thead><tr><th>Original:</th><th>Change:</th></tr></thead><tbody><tr><td markdown="1">

```kotlin
call.respondText("OK")
```

</td><td markdown="1">

```kotlin
call.respondRedirect("/", permanent = false)
```

</td></tr></tbody></table>

## Using the Form authentication

To illustrate how to receive POST parameters we have handled the login manually, but we can also use the authentication
feature with a form provider:

```kotlin
install(Authentication) {
    form("login") {
        userParamName = "username"
        passwordParamName = "password"
        challenge = FormAuthChallenge.Unauthorized
        validate { credentials -> if (credentials.name == credentials.password) UserIdPrincipal(credentials.name) else null }
    }
}
route("/login") {
    get {
        // ...
    }
    authenticate("login") {
        post {
            val principal = call.principal<UserIdPrincipal>()
            call.respondRedirect("/", permanent = false)
        }
    }
}
```

## Sessions

To prevent having to authenticate all the pages, we are going to store the user in a session, and that session will
be propagated to all the pages using a session cookie.

```kotlin
data class MySession(val username: String)

fun Application.module() {
    install(Sessions) {
        cookie<MySession>("SESSION")
    }
    routing {
        authenticate("login") {
            post {
                val principal = call.principal<UserIdPrincipal>() ?: error("No principal")
                call.sessions.set("SESSION", MySession(principal.name))
                call.respondRedirect("/", permanent = false)
            }
        }
    }
} 
```

Inside our pages, we can try to get the session and produce different results:

```kotlin
fun Application.module() {
    // ...
    get("/") {
        val session = call.sessions.get<MySession>()
        if (session != null) {
            call.respondText("User is logged")
        } else {
            call.respond(FreeMarkerContent("index.ftl", mapOf("data" to IndexData(listOf(1, 2, 3))), ""))
        }
    }
}
```

## Using HTML DSL instead of FreeMarker

You can choose to generate HTML directly from the code instead of using a Template Engine.
For that you can use the HTML DSL. This DSL doesn't require installation, but requires an additional artifact (see [HTML DSL] for details).
This artifact provides an extension to respond with HTML blocks:

```kotlin
get("/") { 
    val data = IndexData(listOf(1, 2, 3))
    call.respondHtml {
        head {
            link(rel = "stylesheet", href = "/static/styles.css")
        }
        body {
            ul {
                for (item in data.items) {
                    li { +"$item" }                
                }
            }
        }
    }
}
```

The main benefits of an HTML DSL is that you have full statically typed access to variables and it is thoroughly integrated
with the code base.

The downside of all this is that you have to recompile to change the HTML, and you can't search complete HTML blocks.
But it is lightning fast, and you can use the [autoreload feature](https://jp.ktor.work/servers/autoreload.html) to recompile
on change and reload the relevant JVM classes.

## Exercises

### Exercise 1

Make a registration page and store the user/password datasource in memory in a hashmap.
登録ページを作成して、hashmapにuserとpasswordを格納してみましょう。

### Exercise 2

ユーザーを格納するためにデータベースを使用してみましょう。
Use a database to store the users.
