---
title: 構造化
caption: 複雑なサーバの構築
category: servers
permalink: /servers/structure.html
keywords: routing, routes, structuring, growing, dependency injection, guice, external configuration, 
---

サーバーのコードの複雑さ次第で、なんらかの方法でコードを構造化する必要があるかもしれません。
このページでは、進化に適応しコードをシンプルに保てるような複雑度に応じたコード構造化戦略をいくつか提案します。

**目次:**

* TOC
{:toc}

## Hello World

Ktorで始めるためには、シンプルに`main`関数内で`embeddedServer`を起動すればいいです。

```kotlin
fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) {
        routing {
            get("/") {
                call.respondText("Hello World!")
            }
        }
    }.start(wait = true)
}
```

Ktorがどのように動くのかを理解したり、アプリケーションコードをひと目で確認できるという意味では良い方法です。

## moduleの定義

サーバーを設定するためのコードを拡張関数として抽出することもできます。
これはKtor moduleと呼ばれます:

```kotlin
fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080, module = Application::mainModule).start(wait = true)
}

fun Application.mainModule() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
}
```

## routeの切り出し

コードが成長し始め、より多くのrouteを定義し始めると、main関数をずっと成長させ続けるかわりにコードを分割したいと思うかもしれません。

分割するためのシンプルな方法は、routeを`Routing`クラスをレシーバとした拡張関数に切り出すことです。

サイズに応じて、同じファイル内に定義するか、別ファイルに移動するかを選択できます:

```kotlin
fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080, module = Application::mainModule).start(wait = true)
}

fun Application.mainModule() {
    routing {
        root()
    }
}

// Extracted route
fun Routing.root() {
    get("/") {
        call.respondText("Hello World!")
    }
}
```

`routing { ... }`ブロック内では、暗黙的にthisが`this: Routing`になっています。
そのため`root`メソッドを直接呼び出すことができます。
`this.root()`を呼び出すのと同じ結果になります。
{: .note}

## デプロイと`application.conf`

一度サーバーをデプロイしたあとに、再コンパイルすることなくサーバーの設定を外から追加・変更したいかもしれません。

Ktorライブラリはresourceの`application.conf`ファイルまたは外部ファイルを読み込むいくつかのエントリーポイントを公開しています。
ファイル内ではアプリケーションのエントリーポイントであったり、利用するportであったり、ssl設定であったり、その他任意の設定を定義できます。

[設定ページ](/servers/configuration.html)で、`application.conf`の使い方についてより詳しく見ることができます。

{::comment}
## Dependency injection using Guice

Ktor doesn't impose any dependency injection system. In fact, you can easily write even big applications
without using any.
{:/comment}

## ヘルスチェック

アプリケーション次第で、ヘルスチェックの方法は色々異なる方法を使いたいかもしれません。
最も簡単な方法はHTTP 200 `OK`を返す`/health_check`といった感じのエンドポイントを有効にしておき、必要に応じて依存サービスを検証することです。
全てはあなた次第です。

exceptionをハンドルするために[ステータスページ機能](/servers/features/status-pages.html)を利用することもできます。

```kotlin
install(StatusPages){
    exception<Throwable> { cause ->
        call.respond(HttpStatusCode.InternalServerError)
    }
}
routing {
    get("/health_check") {
        // Check databases/other services.
        call.respondText("OK")
    }
}
```
