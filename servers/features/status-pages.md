---
title: ステータスページ
caption: 例外をハンドリングし、ステータスページをカスタマイズする
category: servers
permalink: /servers/features/status-pages.html
feature:
  artifact: io.ktor:ktor-server-core:$ktor_version
  class: io.ktor.features.StatusPages
redirect_from:
- /features/status-pages.html
ktor_version_review: 1.0.0
---

`StatusPages` Featureを使うとKtorアプリケーションの任意のエラー状態に対して適切にレスポンスを返すことができます。
この機能は標準のアプリケーション設定を使ってインストールされます:

```kotlin
fun Application.main() {
    install(StatusPages)
}
```

StatusPagesから提供される3つのメイン設定オプションがあります:

1. `exceptions` - マッピングされる例外クラスに基づいてレスポンスを設定
2. `status` - ステータスコード値に基づいてレスポンスを設定
3. `statusFile` - クラスパスから標準ファイルレスポンスを設定

{% include feature.html %}

**目次:**

* TOC
{:toc}

## Exceptions

exception設定を使うと、例外を投げる結果となったリクエストに対する単純なインターセプトパターンを使うことができます。
最も基本的なケースとしては、任意の例外に対して500 HTTPステータスコードを設定するものがあります:

```kotlin
install(StatusPages) {
    exception<Throwable> { cause ->
        call.respond(HttpStatusCode.InternalServerError)
    }
}
```

より複雑なユーザインタラクションを実現するために、レスポンスをよりカスタマイズすることができます。

```kotlin
install(StatusPages) {
    exception<AuthenticationException> { cause ->
        call.respond(HttpStatusCode.Unauthorized)
    }
    exception<AuthorizationException> { cause ->
        call.respond(HttpStatusCode.Forbidden)
    }
}
```

これらのカスタマイズは、カスタムステータスコードレスポンスと対になる場合にうまく機能します。
例えば、ユーザが認証されていない場合にログインページを表示する場合などです。

各々の呼び出しは１つのexceptionハンドラによってしかキャッチされないため、
投げられた例外からオブジェクトグラフ的に最も近い例外としてキャッチされます。
同じオブジェクト階層の複数の例外を扱う場合は、１つだけが実行されます。

```kotlin
install(StatusPages) {
    exception<IllegalStateException> { cause ->
        fail("will not reach here")
    }
    exception<ClosedFileSystemException> {
        throw IllegalStateException()
    }
}
intercept(ApplicationCallPipeline.Fallback) {
    throw ClosedFileSystemException()
}
```

１つだけしかハンドリングされないということは、再帰的な呼び出しが行えないことも意味しています。
例えば、以下の設定は`IllegalStateException`をクライアントに伝播する結果になります。

```kotlin
install(StatusPages) {
    exception<IllegalStateException> { cause ->
        throw IllegalStateException("")
    }
}
```

## Exceptionのログ出力

重要なこととして、上で行ったようにハンドラを追加することはrouteで生成した例外を握りつぶすことを意味します。
実際に生成されたエラーを記録するためには、`cause`を手動でログ出力するか、例外を以下のように再throwします。

```kotlin
install(StatusPages) {
    exception<Throwable> { cause ->
        call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
        throw cause
    }
}
```

## Status 

`status`設定はアプリケーション内からのステータスレスポンスに対してカスタムのアクションを定義します。
以下はレスポンステキスト内にステータスコード情報を含める基本となる設定です。

```kotlin
install(StatusPages) {
    status(HttpStatusCode.NotFound) {
        call.respond(TextContent("${it.value} ${it.description}", ContentType.Text.Plain.withCharset(Charsets.UTF_8), it))
    }
}
```

## StatusFile 

`status`設定がレスポンスオブジェクトに対してカスタマイズ可能なアクションを提供する一方、
より汎用的な解決策として訪問者がエラーや認証失敗を確認できるエラーHTMLページを提供するというものがあります。
`statusFile`設定はそのための機能を提供します。

```kotlin
install(StatusPages) {
    statusFile(HttpStatusCode.NotFound, HttpStatusCode.Unauthorized, filePattern = "error#.html")
}
```

このコードはクラスパスから2つのリソースを解決します。

1. 404のとき、error404.htmlを返します。
2. 401のとき、error401.htmlを返します。

`statusFile`設定は文字`#`を設定されたステータスコード値に置換します。

## StatusPagesを使ったリダイレクト
{: #redirect }

`call.respondRedirect("/moved/here", permanent = true)`を使ったリダイレクトを実行する場合、
呼び出し先関数の残りの処理も実行されます。
そのためガード句内でリダイレクトを実行するときには、関数をreturnする必要があります。

```kotlin
routing {
    get("/") {
        if (condition) {
            return@get call.respondRedirect("/invalid", permanent = false)
        }
        call.respondText("Normal response")
    }
}
```

他のフレームワークでは、リダイレクトで例外を使用し正常系フローは実行されないため、
すべてのサブ関数チェーンに戻ることを心配することなく、
ガード句内やサブ関数内でリダイレクトを実行できます。

これをシミュレートするためにStatusPage Featureを使うことができます:


```kotlin
fun Application.module() {
    install(StatusPages) {
        exception<HttpRedirectException> { e ->
            call.respondRedirect(e.location, permanent = e.permanent)
        }
    }
    routing {
        get("/") {
            if (condition) {
                redirect("/invalid", permanent = false)
            }
            call.respondText("Normal response")
        }
    }
}

class HttpRedirectException(val location: String, val permanent: Boolean = false) : RuntimeException()
fun redirect(location: String, permanent: Boolean = false): Nothing = throw HttpRedirectException(location, permanent)
```

[redirect-with-exception](/samples/other/redirect-with-exception.html)サンプル内に、
これについてのより発展的な例が示されています。
