---
title: 自動リロード
caption: 自動リロードで時間節約
category: servers
permalink: /servers/autoreload.html
keywords: autoreload watchpaths
ktor_version_review: 1.0.0
---

開発中は、フィードバックループのサイクルを高速にすることが重要です。
多くの場合、サーバーの再起動には時間がかかることがあるため、Ktorには、アプリケーションクラスをリロードする基本的な自動リロード機能が用意されています。

自動リロードは[Java 9では機能しません](https://github.com/ktorio/ktor/issues/359)。現在のところ使用したい場合はJDK 8を使用してください。
{: .note }
{: #java9 }

自動リロードを使用すると、パフォーマンスが低下します。
そのため、実稼働環境やベンチマークを実行する際には使用しないでください。
{: .note.performance}

**目次:**

* TOC
{:toc}

## class変更時の自動リロード
{: #basics}

[組み込みサーバー](#embedded-server)または[設定ファイル](#configuration-file)のどちらを利用する場合でも、監視する部分文字列のリストを提供する必要があります。

これは監視したいクラスローダーと一致します。

したがって、例えばgradleを使用する場合の典型的なクラスローダーは次のようになります。
`/Users/user/projects/ktor-exercises/solutions/exercise4/build/classes/kotlin/main`

この場合、 `solutions/exercise4` が`exercise4`に一致するため、監視対象のクラスローダーになります。.

## 組み込みサーバーを使う場合
{: #embedded-server}

カスタムのmainメソッドと`embeddedServer`を使用する場合、オプションのパラメーター`watchPaths`を使用して、監視及び自動リロードされるサブパスのリストを指定できます。

```kotlin
fun main(args: Array<String>) {
    embeddedServer(
        Netty,
        watchPaths = listOf("solutions/exercise4"),
        port = 8080,
        module = Application::mymodule
    ).apply { start(wait = true) }
}

fun Application.mymodule() {
    routing {
        get("/plain") {
            call.respondText("Hello World!")
        }
    }
}
```

`watchPaths`を使用する場合、サーバーを構成するためのモジュールににラムダを使用するのではなく、メソッド参照を提供する必要があります。

{: .note}

メソッド参照の代わりにラムダを使用しようとすると、次のエラーが表示されます:

```
Exception in thread "main" java.lang.RuntimeException: Module function provided as lambda cannot be unlinked for reload
```

このエラーを修正するには、次のようにラムダ本体をApplicationの拡張メソッド(モジュール)に抽出するだけです:

{% capture left %}
❌ Code that *won't* work:

```kotlin
fun main(args: Array<String>) {
    // ERROR! Module function provided as lambda cannot be unlinked for reload
    embeddedServer(Netty, watchPaths = listOf("solutions/exercise4"), port = 8080) {
        routing {
            get("/") {
                call.respondText("Hello World!")
            }
        }
    }.start(true)
}
```
{: .error }
{% endcapture %}

{% capture right %}
✅ Code that will work:
```kotlin
fun main(args: Array<String>) {
    embeddedServer(
        Netty, watchPaths = listOf("solutions/exercise4"), port = 8080,
        // GOOD!, it will work 
        module = Application::mymodule
    ).start(true)
}

// Body extracted to a function acting as a module
fun Application.mymodule() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
}
```
{: .success }
{% endcapture %}

{% include two-column.html left=left right=right %}

## `application.conf`を使う場合
{: #configuration-file}

設定ファイルを使用する場合、例えば[`EngineMain`](/servers/engine.html)を使用して、またはコマンドラインから実行する場合はサーバーコンテナ内でホストされます。

この機能を有効にするには`watch`キーを`ktor.deployment`設定に追加します。

`watch` - 監視され、自動的にリロードされるクラスパスエントリの配列

```kotlin
ktor {
    deployment {
        port = 8080
        watch = [ module1, module2 ]
    }
    
    …
}
```

現時点では、watchキーはロードされたクラスパスエントリやjar名、プロジェクトディレクトリ名などに対して`contains`で一致する単なる文字列です。
これらのクラスは、変更が検出されたときにリサイクルされる特別な`ClassLoader`でロードされます。

`ktor-server-core`クラスは特に自動リロードから除外されているため、ktor自体でなにか作業をしている場合は自動的にリロードされると思わないでください。
自動リロードが開始される前にコアクラスがロードされるため、機能しません。
除外は潜在的に小さくすることができますが、起動中にロードされた型のすべての推移的閉包を分析することは困難です。
{: .note}

クラスパスのエントリは`file:///path/to/project/build/classes/myproject.jar`のように見えるため、`to/project`は一致しますが、`com.mydomain`は一致しません。
{: .note}

## ソースの変更時に自動的に再コンパイルする

自動リロード機能はクラスファイルの変更のみを検出するため、アプリケーションを自分でコンパイルする必要があります。

IntelliJ IDEAを実行中に、`Build -> Build Project`で実行できます。

ただし、gradleを使用してソースの変更を自動的に検出し、自動的にコンパイルすることもできます。プロジェクトフォルダー内の別のターミナルで`gradle -t build`を実行するだけです。

これはアプリケーションをコンパイルし、その後追加のソース変更を監視し、必要に応じて再コンパイルします。したがって自動リロードをトリガーします。

その後、別のターミナルを使用して`gradle run`でアプリケーションを実行できます。

## 例
{: #example}

次の例を考えてみましょう:

`build.gradle`を利用するか、IDE内で直接アプリケーションを実行できます。

サンプルファイルでmainメソッドを実行するか、`io.ktor.server.netty.EngineMain.main`を実行します。
`commandLineEnvironment` を使用するEngineMainは `application.conf` ファイルの読み込みをサポートします(HOCON形式)。

{% capture main-kt %}

```kotlin
package io.ktor.exercise.autoreload

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

// Exposed as: `static void io.ktor.exercise.autoreload.MainKt.main(String[] args)`
fun main(args: Array<String>) {
    //io.ktor.server.netty.main(args) // Manually using Netty's EngineMain
    embeddedServer(
        Netty, watchPaths = listOf("solutions/exercise4"), port = 8080,
        module = Application::module
    ).apply { start(wait = true) 
}

// Exposed as: `static void io.ktor.exercise.autoreload.MainKt.module(Application receiver)`
fun Application.module() {
    routing {
        get("/plain") {
            call.respondText("Hello World!")
        }
    }
}
```
{% endcapture %}

{% capture application-conf %}
```kotlin
ktor {
    deployment {
        port = 8080
        watch = [ solutions/exercise4 ]
    }

    application {
        modules = [ io.ktor.exercise.autoreload.MainKt.module ]
    }
}
```
{% endcapture %}

{% include tabbed-code.html
    tab1-title="main.kt" tab1-content=main-kt
    tab2-title="application.conf" tab2-content=application-conf
%}

ご覧の通り、監視したいクラスローダー(この場合は`solutions/exercise4`のみ)に一致する文字列のリストを指定する必要があります。これは変更時に再読込する必要があります。

