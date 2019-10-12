---
title: クイックスタート
caption: クイックスタート
category: quickstart
toc: true
permalink: /quickstart/index.html
children: /quickstart/quickstart/
ktor_version_review: 1.0.0
---

![Ktor logo](/assets/images/ktor_logo.svg){:style="width:134px;height:56px;"}
 
Ktorを使えばWebアプリケーション、HTTPサービス、モバイルアプリ、ブラウザアプリケーションといったネットワーク接続アプリケーションを簡単に構築できます。
モダンなネットワーク接続アプリケーションはユーザに最適な体験を提供するため非同期である必要があり、Kotlinコルーチンはこれを簡単かつ直接的な方法で解決する素晴らしい手段です。

まだまだ道のりは遠いですが、Ktorのゴールはネットワーク接続アプリケーション用のMultiplatformアプリケーションフレームワークになることです。

現在、JVMクライアントとJVMサーバーをサポートしており、また、Javascript, iOS, Androindクライアントも同様にサポートしています。
また、サーバー・クライアントの機能をNativeにおいても利用できるようにしているところです。

**目次:**

* TOC
{:toc}

## Ktorプロジェクトのセットアップ

あなたはKtorプロジェクトを[Maven](/quickstart/quickstart/maven.html)や [Gradle](/quickstart/quickstart/gradle.html)や[start.ktor.io](/quickstart/generator.html#)や[IntelliJ Plugin](/quickstart/quickstart/intellij-idea.html)を使ってセットアップできます。

なお、IntelliJプラグインの場合[start.ktor.io](/quickstart/generator.html#)同様にKtorプロジェクトを作れるだけでなく、IDEとの統合のための便利な追加部分もあります。

### 1) 最初のステップとして、プロジェクトを作成するための設定をし、インストールする機能を選択します
![](/quickstart/quickstart/intellij-idea/plugin/ktor-plugin-1.png){: width="100%" }

### 2) 次のステップとして、プロジェクトの成果物の設定をします
![](/quickstart/quickstart/intellij-idea/plugin/ktor-plugin-2.png){: width="100%" }

それで終わりです。新しいプロジェクトが作成され、IDEにより開かれます。

## Hello World

Ktorによる単純なHello Worldプログラムは以下のようになります。

![Ktor Hello World](/quickstart/1/ktor_hello_world_main.png){: width="100%" }

1. ここに普通のMainメソッドを定義します
2. 次にNettyを使った組み込みサーバをバックエンドとして作成し、8080番ポートをリッスンするようにします。
3. ブロックで所定のパスやHTTPメソッドを指定した上で、Routing機能をインストールします。
4. 実際のルーティングはこの場合`/demo`への*GET request*で、`HELLO WORLD!`というメッセージが返信されます。
5. 実際にサーバーを起動し、接続を待ちます

{% capture main-kt %}
```kotlin
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    val server = embeddedServer(Netty, port = 8080) {
        routing {
            get("/") {
                call.respondText("Hello World!", ContentType.Text.Plain)
            }
            get("/demo") {
                call.respondText("HELLO WORLD!")
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


## アプリケーションへのアクセス方法

mainメソッドがあるなら、IDEでそれを実行することができます。
そうすることでHTTPサーバーが起動され、[http://127.0.0.1:8080](http://127.0.0.1:8080/)がリッスンされます。
あなたのお気に入りのブラウザでそのアドレスを開くことができます。

もしうまくいかなければ、あなたのPC内でそのポートをすでに使っているかもしれません。
8080（上のコード例の10行目で指定している番号）番ポートから別のポートに必要に応じて変えることができます。
{: .note}

![Ktor Hello World Browser](/quickstart/1/screenshot.png){: width="50%""}

この時点であなたはとても単純なWebのバックエンドを動作させており、変更を加えることでその結果をあなたのブラウザ上で見ることができます。

applicationプラグインと`mainClassName`フィールドでGradleプロジェクトの設定を済ませていれば、
ターミナルから`./gradlew run`コマンド（Linux/Macの場合。Windowsは`gradlew run`）でも起動することができます。
{:.note}

{::comment}

## 次のステップ

これで次のステップの準備が整いました。*どんな種類のアプリケーションをあなたは開発しているのでしょうか？*

1. [RESTful API: *data class*をJSONとして提供する方法](/quickstart/restful.html)
2. Webアプリケーション:
    * [HTMLをkotlinx.htmlを使ったDSLで型安全に配信する方法](/quickstart/html-dsl.html)
    * [HTMLをFreeMarkerテンプレートエンジンを使って配信する方法](/quickstart/html-freemarker.html)
    
{:/comment}

## 関連記事

{% include category-list.html %}
