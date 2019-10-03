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

テンプレートに加えて、静的コンテンツも配信したいでしょう。
静的コンテンツはKtorにより高速に配信され、また途中でやめたダウンロードを再開したり、部分的にファイルをダウンロードしたりできるようになるPartial Contentなどの他のFeatureと互換性があります。

それでは、簡単な`style.css`ファイルを先ほど作成したページにstyleを適用するために配信してみましょう。

静的ファイルを配信するために新しいFeatureをインストール必要はなく、簡単なRoute handlerで実現できます。
`/resources/static`に置かれた静的ファイルを`/static` urlで配信するためには、次のようなコードを書けばよいでしょう:

```kotlin
routing {
    // ...
    static("/static") {
        resources("static")
    }
}
```

次に以下の内容の`resources/static/style.css`を作りましょう:

```css
body {
    background: #B9D8FF;
}
```

これに加えて、`style.css`ファイルを含むために先ほどのtemplateファイルを更新する必要があります:
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

最終的にこうなります:

![](/quickstart/guides/website/website2.png){:.rounded-shadow}

1990年来のカラフルなWebサイトができましたね!

静的ファイルはテキストファイルだけではありません! 画像(派手なアニメーションで点滅してるようなgifファイルなんかどうですか? 👩🏻‍🎨)を`static`フォルダに追加してから、HTMLテンプレートに`<img src="...">`タグを追記してみましょう!
{: .note.exercise}

## コンテンツの分割を可能にする: 大きなファイルや動画など

今回のケースでは必要ありませんが、もしコンテンツの分割配信のサポートを有効にすると、
頻繁に問題が発生するネットの接続状況において大きな静的ファイルの配信を再開したり、動画の提供や視聴をすることができます。

コンテンツの分割配信の有効化は簡単です:

```kotlin
install(PartialContent) {
}
```

## form画面の作成 

次にニセのログインフォームを作成していきます。単純にするためにすべてのユーザーがusernameと同じパスワードでログインできるようにし、またユーザー登録画面については実装しません。

`resources/templates/login.ftl`を以下の内容で作成してください:

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


テンプレートに加えて,いくつかロジックを追加する必要があります。今回のケースではGETとPOSTメソッドをいくつかのコード片として処理していきます。

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

既に書いたとおり、任意の`username`に対してユーザーネームと同じ`password`でログインできるようにしますが、nullについては受け入れないようにします。
もしログインに成功すれば、今のところただの`OK`の文字列で応答します。一方もしログインに失敗すればエラーと共にログインの時と同じフォームを再利用します。

## リダイレクション 

ルートリファクタリングやフォームのようないくつかのケースでは、レダイレクションを行いたいことがあります(一時的、永続的問わず)。今回のケースではログインに成功した場合、平文を返す代わりに、一時的にhomeページにリダイレクトしたいです。

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
