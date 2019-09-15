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

## Calls and pipelines

In ktor a pair of incoming request and response (complete or incomplete)
is named _application call_
([ApplicationCall](/servers/calls.html)).
Every application _call_ is passed through an _application call pipeline_
([ApplicationCallPipeline](https://api.ktor.io/latest/io.ktor.application/-application-call-pipeline/index.html))
consisting of several (or none) _interceptors_. Interceptors are invoked one by one and every _interceptor_
can amend request or response and control pipeline execution by proceeding (`proceed()`) to the next interceptor
or finishing (`finish()` or `finishAll()`) the whole pipeline execution
(so the next interceptor is not invoked,
see [PipelineContext](https://api.ktor.io/latest/io.ktor.util.pipeline/-pipeline-context/index.html) for details).
It also can decorate the remaining interceptors chain doing additional actions before and after `proceed()` invocation.

Consider the following decorating example:

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

A pipeline may consist of several _phases_. Every interceptor is registered at a particular phase.
So interceptors are executed in their phases order. See [Pipelines](/advanced/pipeline) documentation
for the more detailed explanation.

## Application call

An application call consists of a pair of request with response and a set of parameters.
So an application call pipeline has a pair of _receive_ and _send_ pipelines. The request's content (body)
 could be received using `ApplicationCall.receive<T>()` where `T` is an expected type of content.
 For example, `call.receive<String>()` reads requests body as a `String`. Some types could be received with no
 additional configuration
 out of the box while receiving a custom type may require a feature installation or configuration.
 Every `receive()` causes receive pipeline (`ApplicationCallPipeline.receivePipeline`)
 to be executed from the beginning so every receive pipeline interceptor could transform or by-pass request body.
 The original body object type is `ByteReadChannel` (asynchronous byte channel).

An application response body could be provided by `ApplicationCall.respond(Any)` function invocation that
executes a response pipeline (`ApplicationCallPipeline.respondPipeline`). Similar to receive pipeline,
every response pipeline interceptor could transform a response object. Finally, a response object should be
converted into an instance of
[OutgoingContent](https://api.ktor.io/latest/io.ktor.http.content/-outgoing-content/index.html).

A set of extension functions `respondText`, `respondBytes`, `receiveText`, `receiveParameters` and so on
simplifies request and response objects construction.

## Routing

An empty application has no interceptors so 404 Not Found will be generated for every request.
 An application call pipeline should be intercepted to handle requests. Such interceptor could respond depending on
request URI like this:

```kotlin
intercept {
    val uri = call.request.uri
    when {
        uri == "/" -> call.respondText("Hello, World!")
        uri.startsWith("/profile/") -> { TODO("...") }
    }
}
```

For sure, this approach has a lot of disadvantages.
Fortunately, there is the [Routing](/servers/features/routing.html) feature for structured requests
handling that does intercept application call pipeline and provides a way to register handlers for _routes_.
Since the only thing Routing does is intercepting the application call pipeline, manual intercepting with Routing also works.
Routing consists of a tree of routes having handlers and interceptors. A set of extension functions in ktor
provides an easy way to register handlers like this:

```kotlin
routing {
    get("/") {
        call.respondText("Hello, World!")
    }
    get("/profile/{id}") { TODO("...") }
}
```

Notice that routes are organized into a tree so you can declare structured routes:

```kotlin
routing {
    route("profile/{id}") {
        get("view") { TODO("...") }
        get("settings") { TODO("...") }
    }
}
```

A routing path can contain constant parts and parameters such as `{id}` in the example above.
Property `call.parameters` provides access to the captured setting values.

## Content Negotiation

[ContentNegotiation](/servers/features/content-negotiation.html) feature provides a way to negotiate
mime types and convert types using [Accept](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept)
 and [Content-Type](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Type) headers.
A content converter could be registered for a particular content type for receiving and responding objects.
There are [Jackson](/servers/features/content-negotiation/jackson.html),
 [Gson](/servers/features/content-negotiation/gson.html) and [kotlinx.serialization](https://jp.ktor.work/servers/features/content-negotiation/serialization-converter.html)
 content converters available out of the box that could be plugged into the feature.

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

- [Quick start](/quickstart/index.html) for creating your first ktor server application
- [What is an Application?](/servers/application.html)
- [Application calls](/servers/calls.html)
- [Application lifecycle explanation](/servers/lifecycle.html)
- [Application configuration](/servers/configuration.html)
- [Routing](/servers/features/routing.html)
- [Pipelines exlained](/advanced/pipeline)
