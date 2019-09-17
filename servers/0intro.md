---
title: イントロダクション
category: servers
permalink: /servers/index.html
caption: サーバーアプリケーション
ktor_version_review: 1.2.1
---

# イントロダクションとキーコンセプト

**目次:**

* TOC
{:toc}

## ApplicationとApplicationEnvironment

Ktorアプリケーションの動作しているインスタンスは[Application](https://api.ktor.io/latest/io.ktor.application/-application/index.html)によって表現されます。
Ktorアプリケーションは1つ以上の[_モジュール_](/servers/application.html#modules)によって構成されます。
各モジュールはKotlinのlamdaまたはfunctionです。(通常Applicationインスタンスをレシーバーまたは引数として保持します)

アプリケーションは [ApplicationEnvironment](https://api.ktor.io/latest/io.ktor.application/-application-environment/index.html)によって表現される _アプリケーション環境_ 内で起動されます。
ApplicationEnvironmentは _アプリケーション設定_ も含みます。
 (詳細は[設定](/servers/configuration.html)ページをご覧ください)。

Ktorの _サーバー_ はアプリケーション環境で起動され、アプリケーションのライフサイクルを制御します。
ライフサイクルとはつまり、環境によるアプリケーションインスタンスの生成・削除です。
生成は遅延されるのか、[ホットリロード](/servers/autoreload.html)機能があるのかなどは実装によります。
そのためアプリケーションの停止はサーバの停止自体を意味するとは限りません。例えばサーバが動き続けるとアプリケーションがリロードされることもあります。

アプリケーションモジュールはアプリケーションが起動された際に１つ１つ起動され、どのモジュールもアプリケーションのインスタンスを設定できます。アプリケーションインスタンスは _feature_ のインストールおよび _パイプライン_ のインターセプトにより設定ができます。

詳細は [Lifecycle](/servers/lifecycle.html) をご覧ください。

## Feature

_feature_ とはapplicationにプラグイン可能な特定機能を指します。
通常、リクエストとレスポンスを _受信_ し、そこで特定の機能を実行します。
例えば[デフォルトヘッダー](/servers/features/default-headers.html)機能はレスポンスを受信し、`Date`, `Server`ヘッダーを追加します。
`install`関数によって以下コードのようにapplicationに機能がインストールできます。

```kotlin
application.install(DefaultHeaders) {
   // featureの設定
}
```

設定を行うlambda関数はいくつかの機能に対しては任意で必要になります。
featureは1度だけしかインストールされませんが、そういったケースに置いては設定のその際に設定の適用が必要となります。
そういった機能においては、例えば`routing {}` のような、featureがインストールされていない場合はインストールもしつつ設定を適用するヘルパー関数があります。

## コールとパイプライン

Ktorにおいては、リクエストとレスポンス(完了しているか否かに関わらない)のペアは _アプリケーションコール_ ([ApplicationCall](/servers/calls.html)) と呼ばれています。
全てのアプリケーションコールは複数(または0個)の _インターセプター_ で構成される _アプリケーションコールパイプライン_ ([ApplicationCallPipeline](https://api.ktor.io/latest/io.ktor.application/-application-call-pipeline/index.html))を介して渡されます。

インターセプターは1つずつ呼び出され、すべてのインターセプターは要求または応答を修正し、次のインターセプターに進む(`proceed()`)またはパイプラインの実行全体を終了する(`finish()`または`finishAll()`)ことでパイプラインの実行を制御できます
(その場合次のインターセプターは呼び出されません。詳細については、[PipelineContext](https://api.ktor.io/latest/io.ktor.util.pipeline/-pipeline-context/index.html)を参照してください)。 
また、`proceed()`呼び出しの前後に追加のアクションを実行することで、残りのインターセプターチェーンに処理を追加することができます。

次の例を考えてみましょう。

```kotlin
intercept {
    myPrepare()
    try {
        proceed()
    } finally {
        myComplete()
    }
}
```

パイプラインはいくつかの _フェーズ_ で構成され、すべてのインターセプターは特定のフェーズで登録されます。
そのため、インターセプターはフェーズ順に実行されます。 
より詳細な説明については、[パイプライン](/advanced/pipeline)のドキュメントを参照してください。

## アプリケーションコール

アプリケーションコールは、リクエスト・レスポンスとパラメーターのペアで構成されます。
したがって、アプリケーションコールパイプラインには、リクエストパイプラインとレスポンスパイプラインのペアがあります。
要求されたコンテンツ(body)は、`ApplicationCall.receive<T>()`を使用して受け取ることができます(Tは予想されるコンテンツの型です)。
たとえば、`call.receive<String>()`はリクエストのbodyを文字列として読み取ります。
一部の型は追加設定なしですぐに受信できますが、独自の型を受信するにはFeatureのインストールまたは設定が必要になる場合があります。
受信パイプライン(`ApplicationCallPipeline.receivePipeline`)が最初から実行され、`receive()`が実行されるため、すべての受信パイプラインインターセプターがリクエストボディを変換またはバイパスできます。
元のbodyオブジェクトタイプはByteReadChannel(非同期バイトチャネル)です。

アプリケーションレスポンス本文は、レスポンスパイプライン(`ApplicationCallPipeline.respondPipeline`)を実行する`ApplicationCall.respond(Any)`関数呼び出しによって提供できます。
受信パイプラインと同様に、すべてのレスポンスパイプラインインターセプターはレスポンスオブジェクトを変換できます。
最後に、応答オブジェクトを[OutgoingContent](https://api.ktor.io/latest/io.ktor.http.content/-outgoing-content/index.html)のインスタンスに変換する必要があります。

拡張関数respondText、respondBytes、receiveText、receiveParametersなどのセットにより、リクエストおよびレスポンスオブジェクトの構築が簡単になります。

## ルーティング

空のアプリケーションにはインターセプターがないため、リクエストごとに404 Not Foundが生成されます。
要求を処理するには、アプリケーション呼び出しパイプラインをインターセプトする必要があります。
以下のインターセプターは、次のようなリクエストURIに応じて応答できます。

```kotlin
intercept {
    val uri = call.request.uri
    when {
        uri == "/" -> call.respondText("Hello, World!")
        uri.startsWith("/profile/") -> { TODO("...") }
    }
}
```

確かに、このアプローチには多くの欠点があります。
幸いなことに、アプリケーション呼び出しパイプラインをインターセプトし、ルートのハンドラーを登録する方法を提供する、構造化された要求処理のための[ルーティング](/servers/features/routing.html)機能があります。
ルーティングが行うのはアプリケーション呼び出しパイプラインをインターセプトすることだけなので、ルーティングを使用した手動インターセプトも機能します。
ルーティングは、ハンドラーとインターセプターを持つルートのツリーで構成されます。
ktorの一連の拡張関数を使用すると、次のようなハンドラーを簡単に登録できます。

```kotlin
routing {
    get("/") {
        call.respondText("Hello, World!")
    }
    get("/profile/{id}") { TODO("...") }
}
```

ルートはツリー構造になっているため、構造化されたルートを宣言できます。

```kotlin
routing {
    route("profile/{id}") {
        get("view") { TODO("...") }
        get("settings") { TODO("...") }
    }
}
```

ルーティングパスには、上記の例の`{id}`などの定数部分とパラメーターを含めることができます。
プロパティ`call.parameters`は、キャプチャされた設定値へのアクセスを提供します。

## コンテンツナビゲーション

[コンテンツナビゲーション](/servers/features/content-negotiation.html)機能は、[Accept](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept)および[Content-Type](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Type)ヘッダーを使用して、MIMEタイプをネゴシエートし、タイプを変換する方法を提供します。
コンテンツコンバータは、オブジェクトを受信および応答する特定のコンテンツタイプに対して登録できます。
[Jackson](/servers/features/content-negotiation/jackson.html)、[Gson](/servers/features/content-negotiation/gson.html)、および[kotlinx.serialization](https://jp.ktor.work/servers/features/content-negotiation/serialization-converter.html)コンテンツコンバーターがあり、この機能にプラグインできます。

例:

```kotlin
install(ContentNegotiation) {
    gson {
        // Configure Gson here
    }
}
routing {
    get("/") {
        call.respond(MyData("Hello, World!"))
    }
}
```

## 関連項目

- [クイックスタート](/quickstart/index.html) は最初のKtor サーバーアプリケーションを作る手助けになります。
- [Applicationとは?](/servers/application.html)
- [Application call](/servers/calls.html)
- [Applicationライフサイクル](/servers/lifecycle.html)
- [Application設定](/servers/configuration.html)
- [Routing](/servers/features/routing.html)
- [Pipeline](/advanced/pipeline)

