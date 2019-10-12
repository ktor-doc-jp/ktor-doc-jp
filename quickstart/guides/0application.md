---
title: First App
caption: 最初のアプリケーションの作成
category: quickstart
permalink: /quickstart/guides/application.html
redirect_from:
- /quickstart/application.html
ktor_version_review: 1.0.0
---

このチュートリアルではHTTPリクエストに対し`Hello, World!`と返す単純なKtorのサーバアプリケーションの作り方について学びます。
Ktorアプリケーションは[Maven](/quickstart/quickstart/maven.html)や[Gradle](/quickstart/quickstart/gradle.html)といった一般的なビルドシステムで構築することができます。

**目次:**

* TOC
{:toc}

## 正しい依存性を設定
{: #dependencies }

Ktorはいくつかのアーティファクトに別れています。
そのため必要な機能だけを利用することができます。
それにより全コードを含んだfat-jarのサイズを減らし、起動時間を短縮できます。

以下のケースでは、`ktor-server-netty`のアーティファクトのみ利用します。
利用可能なアーティファクト一覧は[アーティファクト](/quickstart/artifacts.html)ページをご覧ください。

リリースバージョンのそれらアーティファクトは、jcenter、maven centralリポジトリにあります。
プレリリースのものは[Bintray kotlin/ktor](https://bintray.com/kotlin/ktor)にあります。

ビルドファイルについてのより詳細なガイドは以下をご参照ください:

* [Gradleビルドの設定](/quickstart/quickstart/gradle.html)
* [Mavenビルドの設定](/quickstart/quickstart/maven.html)

## セルフホスティングアプリケーションの作成
{: #self-hosted}

KtorはTomcatのようなServlet互換アプリケーションサーバ、Jetty, Netty, CIOのような組み込みアプリケーションサーバ上で起動できます。

このチュートリアルでは、Nettyを使ったセルフホスティングアプリケーションの作成を行います。

`embeddedServer`関数を呼び出すことで起動することができます。
engineのfactoryメソッドを第一引数に渡し、ポート番号を第二引数に渡し、実際のアプリケーションコードを第四引数に渡します。
（第三引数はhostであり、デフォルト値は`0.0.0.0`になっています）

以下のコードは`GET`で`/`のURLを受け付け`Hello, world!`の文字列を返す、1つのルーティングのみを定義しています。

ルーティングの定義後、`server.start`メソッドを呼び出すことでサーバの起動が可能です。
その際にboolean引数を渡すことでアプリケーションのmainスレッドをブロックするかどうかを指定します。

{% capture main-kt %}
```kotlin
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    val server = embeddedServer(Netty, 8080) {
        routing {
            get("/") {
                call.respondText("Hello, world!", ContentType.Text.Html)
            }
        }
    }
    server.start(wait = true)
}
```
{% endcapture %}

{% include tabbed-code.html
    tab1-title="Main.kt" tab1-content=main-kt
    no-height="true"
%}

&nbsp;

サーバが起動後、HTTPリクエストを受け付けるのみでそれ以外のことを行わないのであれば、`wait = true`引数でserver.startを呼び出すのがいいでしょう。
{: .note}

## アプリケーションの起動
{: #running }

アプリケーションのエントリーポイントはKotlinの標準的な`main`関数になっているため、単純にそれを起動することでサーバの起動および指定のポートのリッスンがスタートします。

`localhost:8080`ページをあなたのブラウザで確認すれば、`Hello, world!`というテキストが確認できるでしょう。

## 次のステップ
{: #next-steps }

以下はセルフホスティングKtorアプリケーションの構築と起動に関する単純な例です。
サーバーとしてのKtorについて学ぶために以下が推奨されます:

* [アプリケーションとは何か？](/servers/application.html)
* [フィーチャー](/features)
* [アプリケーション構造](/servers/structure.html)
* [テスト](/servers/testing.html)
