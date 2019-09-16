---
title: イントロダクション
category: servers
permalink: /servers/index.html
caption: サーバーアプリケーション
ktor_version_review: 1.2.1
---

この文章は機械翻訳を利用して翻訳されています。翻訳してくださる有志の方をお待ちしております。
{: .note.machine-translation}

# イントロダクションとキーコンセプト

**目次:**

* TOC
{:toc}

## ApplicationとApplicationEnvironment

Ktorアプリケーションの動作しているインスタンスは[Application](https://api.ktor.io/latest/io.ktor.application/-application/index.html)によって表現されます。
Ktor _application_ は[_module群_](/servers/application.html#modules)（１つだけのこともある）によって構成されます。
各moduleはKotlinのlambdaまたはfunctionです。（通常applicationインスタンスをreceiverまたはparameterとして保持します）

applicationは [ApplicationEnvironment](https://api.ktor.io/latest/io.ktor.application/-application-environment/index.html)によって表現される _environment_ 内で起動されます。
ApplicationEnvironmentは _application config_ も含みます。
 (詳細は[Configuration](/servers/configuration.html)ページをご覧ください)。
 
Ktorの _server_ はenvironmentとともに起動され、applicationのライフサイクルを制御します。
ライフサイクルとはつまり、環境によるapplicationインスタンスの作成・削除です。
遅延作成なのか、[hot reload](/servers/autoreload.html)機能があるのかなどは実装によります。
そのためapplicationの停止はサーバの停止自体を意味するとは限りません。例えばサーバが動き続けるとapplicationがリロードされることもあります。

application moduleはapplicationが起動された際に１つ１つ起動され、どのmoduleもapplicationのインスタンスを設定できます。
applicationインスタンスは _feature_ のインストールおよび _pipeline_ のインターセプトにより設定ができます。

詳細は [lifecycle](/servers/lifecycle.html) をご覧ください。

## Feature

_feature_ とはapplicationにプラグイン可能な特定機能を指します。
通常、リクエストとレスポンスを _intercept_ し、そこで特定の機能を実行します。
例えば[Default Headers](/servers/features/default-headers.html)機能はレスポンスをinterceptし、`Date`, `Server`ヘッダーを追加します。
`install`関数によって以下コードのようにapplicationに機能がインストールできます。

```kotlin
application.install(DefaultHeaders) {
   // configure feature
}
```

設定を行うlambda関数はいくつかの機能に対しては任意で必要になります。
featureは1度だけしかインストールされませんが、そういったケースに置いては設定のその際に設定の適用が必要となります。
そういった機能においては、例えば`routing {}` のような、featureがインストールされていない場合はインストールもしつつ設定を適用するヘルパー関数があります。

## Callとpipeline

Ktorにおいては、リクエストとレスポンス（完了しているか否かに関わらない）のペアは _application call_ ([ApplicationCall](/servers/calls.html)) と呼ばれています。
全てのapplication callは複数（または0個）の _interceptor_ で構成される _application call pipeline_ ([ApplicationCallPipeline](https://api.ktor.io/latest/io.ktor.application/-application-call-pipeline/index.html))を介して渡されます。

インターセプターは1つずつ呼び出され、すべてのインターセプターは要求または応答を修正し、次のインターセプターに進む（`proceed（）`）またはパイプラインの実行全体を終了する（`finish（）`または`finishAll（）`）ことでパイプラインの実行を制御できます
（その場合次のインターセプターは呼び出されません。詳細については、[PipelineContext](https://api.ktor.io/latest/io.ktor.util.pipeline/-pipeline-context/index.html)を参照してください）。 
また、`proceed（）`呼び出しの前後に追加のアクションを実行することで、残りのインターセプターチェーンに追加処理を加えることができます。

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

パイプラインはいくつかの _phaes_ で構成されます。
すべてのインターセプターは特定のフェーズで登録されます。
そのため、インターセプターはフェーズ順に実行されます。 
より詳細な説明については、[Pipelines](/advanced/pipeline)のドキュメントを参照してください。

## Application call

application callは、リクエスト・レスポンスとパラメーターセットのペアで構成されます。
したがって、application callパイプラインには、受信パイプラインと送信パイプラインのペアがあります。
要求のコンテンツ（body）は、ApplicationCall.receive <T>（）を使用して受信できます（Tは予想されるコンテンツのタイプです）。
たとえば、call.receive <String>（）はリクエストのbodyを文字列として読み取ります。
一部のタイプは追加設定なしですぐに受信できますが、カスタムタイプを受信するにはFeatureのインストールまたは設定が必要になる場合があります。
すべての`receive（）`により、受信パイプライン（`ApplicationCallPipeline.receivePipeline`）が最初から実行されるため、
すべての受信パイプラインインターセプターがリクエストボディを変換またはバイパスできます。
元のbodyオブジェクトタイプはByteReadChannel（非同期バイトチャネル）です。

アプリケーションレスポンス本文は、レスポンスパイプライン（`ApplicationCallPipeline.respondPipeline`）を実行する
`ApplicationCall.respond（Any）`関数呼び出しによって提供できます。
受信パイプラインと同様に、すべての応答パイプラインインターセプターは応答オブジェクトを変換できます。
最後に、応答オブジェクトを[OutgoingContent](https://api.ktor.io/latest/io.ktor.http.content/-outgoing-content/index.html)のインスタンスに変換する必要があります。

拡張関数respondText、respondBytes、receiveText、receiveParametersなどのセットにより、リクエストおよびレスポンスオブジェクトの構築が簡単になります。

## Routing

空のアプリケーションにはインターセプターがないため、リクエストごとに404 Not Foundが生成されます。
要求を処理するには、アプリケーション呼び出しパイプラインをインターセプトする必要があります。
このようなインターセプターは、次のようなリクエストURIに応じて応答できます。

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
幸いなことに、アプリケーション呼び出しパイプラインをインターセプトし、ルートのハンドラーを登録する方法を提供する、構造化された要求処理のための[Routing](/servers/features/routing.html)機能があります。
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

## Content Negotiation

[ContentNegotiation](/servers/features/content-negotiation.html)機能は、
[Accept](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept)および
[Content-Type](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Type)ヘッダーを使用して、
MIMEタイプをネゴシエートし、タイプを変換する方法を提供します。
コンテンツコンバータは、オブジェクトを受信および応答する特定のコンテンツタイプに対して登録できます。
[Jackson](/servers/features/content-negotiation/jackson.html)、[Gson](/servers/features/content-negotiation/gson.html)、
および[kotlinx.serialization](https://jp.ktor.work/servers/features/content-negotiation/serialization-converter.html)コンテンツコンバーターがあり、
この機能にプラグインできます。

Example:

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

## What's next

- [クイックスタート](/quickstart/index.html) for creating your first ktor server application
- [Applicationとは?](/servers/application.html)
- [Application call](/servers/calls.html)
- [Applicationライフサイクル](/servers/lifecycle.html)
- [Application設定](/servers/configuration.html)
- [Routing](/servers/features/routing.html)
- [Pipeline](/advanced/pipeline)
