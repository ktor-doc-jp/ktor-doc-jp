---
title: ライフサイクル
category: servers
permalink: /servers/lifecycle.html
caption: サーバーで何が起きているのか？
---

Ktorは柔軟で拡張性があるように設計されています。
そのため小さく、シンプルな部品で構成されていますが、もしあなたが何が起きているのか理解していないならばブラックボックスのように見えるでしょう。

このセクションでは、Ktorが内部で何を行っているかを探索し、その汎用的なインフラストラクチャとしての性質について学んでいきましょう。

**目次:**

* TOC
{:toc}

## エントリーポイント

Ktorアプリケーションをいくつかの方法で起動することができます:

* `embeddedServer`を呼び出す`main`関数
* `EngineMain` `main` 関数を起動し、[HOCON `application.conf` 設定ファイルを利用](/servers/configuration.html)
* [Webサーバ内でサーブレットとして起動](https://github.com/ktorio/ktor-samples/tree/master/deployment)
* [`ktor-server-test-host`](https://github.com/ktorio/ktor/tree/master/ktor-server/ktor-server-test-host)アーティファクトから`withTestApplication`を使いテストの一部として起動

## 起動

### 共通

**[`ApplicationEngineEnvironment`](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-host-common/jvm/src/io/ktor/server/engine/ApplicationEngineEnvironment.kt):**

起動するためにはイミュータブルな環境を構築する必要があります。
クラスローダー、ロガー、[設定](/servers/configuration.html)、
アプリケーションイベントのイベントバスとして起動するmonitor、
コネクター群、モジュール群を設定し、それらがアプリケーションや[watchPath](/servers/autoreload.html)を形成します。

`ApplicationEngineEnvironmentBuilder`や、お手軽なDSL関数である`applicationEngineEnvironment`, `commandLineEnvironment`を使うことで構築ができます。

**[`ApplicationEngine`](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-host-common/jvm/src/io/ktor/server/engine/ApplicationEngine.kt):**

複数種類の`ApplicationEngine`があります。
Netty, Jetty, CIO or Tomcatといったサーバーをサポートしています。

ApplicationEngineはアプリケーションを起動する役割をもったクラスであり、
特定の**configuration**と、起動・停止可能な**environment**を持っています。

特定のアプリケーションエンジンを起動したとき、
configurationが使われ、
正しいport、hostをSSLや証明書を使い、指定したworkerでリッスンすることができます。

コネクターは特定のhttp/httpsホスト・ポートをリッスンするために使われており、
一方`Application`パイプラインはリクエストをハンドルするために使われています。

**[Application](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-core/jvm/src/io/ktor/application/Application.kt)パイプライン**:

Applicationは`ApplicationEngineEnvironment`によって作成され、初めは空の状態です。
コンテキストとしての`ApplicationCall`を持たないパイプラインになります。
environmentの作成時にアプリケーションの設定を行うため各指定されたmoduleが呼び出されます。

### embeddedServer

main関数を起動し`embeddedServer`関数を呼び出すとき、
特定の`ApplicationEngineFactory`を渡すことで`ApplicationEngineEnvironment`が作成または渡されます。

### EngineMain

Ktorは各サポートしているサーバーエンジンごとに`EngineMain`クラスを定義しています。
このクラスはアプリケーションを起動することができる`main`関数を定義しています。
また、`commandLineEnvironment`を使うことで、
[HOCON `application.conf`](/servers/configuration.html)ファイルをresourceから読み込み、
どのmoduleをインストールするかやサーバーをどう設定するかについて追加の引数を使って指定することができます。

`CommandLine.kt`ソースファイル内に通常これらのクラスは宣言されています。

* CIO: [`io.ktor.server.cio.EngineMain.main`](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-cio/jvm/src/io/ktor/server/cio/EngineMain.kt)
* Jetty: [`io.ktor.server.jetty.EngineMain.main`](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-jetty/jvm/src/io/ktor/server/jetty/EngineMain.kt)
* Netty: [`io.ktor.server.netty.EngineMain.main`](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-netty/jvm/src/io/ktor/server/netty/EngineMain.kt)
* Tomcat: [`io.ktor.server.tomcat.EngineMain.main`](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-tomcat/jvm/src/io/ktor/server/tomcat/EngineMain.kt)

### [TestApplicationEngine](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-test-host/jvm/src/io/ktor/server/testing/TestApplicationEngine.kt)

テストのため、Ktorは`TestApplicationEngine`と、`withTestApplication`という便利なメソッドを定義しています。
それを使うことでアプリケーションmoduleやパイプラインやその他の機能を、サーバーを実際に起動したり何かしらをモックしたりすることなしに、テストすることができます。
そこではインメモリの設定として`MapApplicationConfig("ktor.deployment.environment" to "test")`を使用し、
テスト環境内で実行することを決めています。

## イベントの監視

環境に紐づく形で、monitorインスタンスがあり、Ktorはそれをアプリケーションイベントを生成するために使っています。
monitorインスタンスをイベントをサブスクライブするために使うことができます。
例えば、アプリケーション停止イベントをサブスクライブすることで、特定サービスのシャットダウンや何らかのリソースの終了などを実行することができます。

[Ktorが定義するイベント一覧](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-core/jvm/src/io/ktor/application/ApplicationEvents.kt):  

```kotlin
val ApplicationStarting = EventDefinition<Application>()
val ApplicationStarted = EventDefinition<Application>()
val ApplicationStopPreparing = EventDefinition<ApplicationEnvironment>()
val ApplicationStopping = EventDefinition<Application>()
val ApplicationStopped = EventDefinition<Application>()
```

## [Pipelines](https://github.com/ktorio/ktor/blob/master/ktor-utils/common/src/io/ktor/util/pipeline/Pipeline.kt)

Ktorは非同期で拡張可能な処理を行うためのパイプラインを定義します。
パイプラインはKtor全体で利用されています。

すべてのパイプラインは**subject**の型、**context**の型、**インターセプタ**が関連付けられている**phase**のリストに関連づけられます。
As well as, **attributes** that act as a small typed object container.

フェーズは順序付けられており、どのPhaseよりも先なのか後なのかあるいは最後なのかといった実行順序について定義されています。

各パイプラインは順序付けられたPhaseのリストを持っており、さらに各Phaseごとにinterceptorのセットを持っています。

For example:

* Pipeline
    * Phase1
        * Interceptor1
        * Interceptor2
    * Phase2
        * Interceptor3
        * Interceptor4

あるPhaseのためのインターセプタは、同じPhaseの他のインターセプタには依存していませんが、前のPhaseのインターセプタには依存しています。

パイプラインの実行時、すべての登録されているインターセプタはPhaseで定義されている順序で実行されます。

### [ApplicationCallPipeline](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-core/jvm/src/io/ktor/application/ApplicationCallPipeline.kt)

Ktorのサーバー部分は`ApplicationCallPipeline`を定義しており、`ApplicationCall`をコンテキストとして持っています。
`Application`インスタンスは`ApplicationCallPipeline`インスタンスでもあります。

サーバーのアプリケーションエンジンがHTTPリクエストをハンドルするとき、`Application`パイプラインが実行されます

コンテキストのクラスである`ApplicationCall`はApplicationや`request`、`response`、`attributes`、`parameters`などを保持しています。

最終的にアプリケーションmoduleは、アプリケーションパイプラインの特定のフェーズにインターセプタの登録するのを終了し、
`request`の処理と`response`の送信を行います。

`ApplicationCallPipeline`は以下のビルトインPhaseをパイプラインに定義しています:

```kotlin
val Setup = PipelinePhase("Setup") // Phase for preparing the call, and processing attributes
val Monitoring = PipelinePhase("Monitoring") // Phase for tracing calls: logging, metrics, error handling etc. 
val Features = PipelinePhase("Features") // Phase for infrastructure features, most intercept at this phase
val Call = PipelinePhase("Call") // Phase for processing a call and sending a response
val Fallback = PipelinePhase("Fallback") // Phase for handling unprocessed calls
```

## [Features](/advanced/features)

Ktorは[`ApplicationFeature`](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-core/jvm/src/io/ktor/application/ApplicationFeature.kt) クラスでアプリケーションの機能を定義しています。
Featureは指定のパイプラインに`install`可能なものです。
Featureはパイプラインにアクセスし、interceptorに登録され、様々な種類のことを実行します。

## Routing

Featureやパイプラインツリーがどのようにともに動作しているのかを説明するには、[Routing](/servers/features/routing.html)がどのように動くのかを見るといいでしょう。

Routingは他の機能同様、通常以下のようにインストールされます:

```kotlin
install(Routing) { }
```

登録・起動するための簡単なメソッドも用意されており、それを使うことでinstallを行うことができます。

```kotlin
routing { }
```

Routingはツリーとして定義されます。
各ノードは`Route`であり、`ApplicationCallPipeline`の分離されたインスタンスでもあります。
そのため、ルートのroutingノードが実行されたとき、それ自体のパイプラインが実行されます。
そしてパイプラインの実行が終わったときには、routeが実行され終わっています。

## 関連記事

- [Application calls](/servers/calls.html)
- [Application configuration](/servers/configuration.html)
- [Pipelineについて](/advanced/pipeline)
