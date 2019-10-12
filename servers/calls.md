---
title: Call
caption: ApplicationCall
keywords: calls requests responses
category: servers
permalink: /servers/calls.html
children: /servers/calls/
ktor_version_review: 1.0.0
---

ルートを処理するとき、またはパイプラインを直接インターセプトするとき、ApplicationCallでコンテキストを取得します。

`ApplicationCall`は、2つの主要なプロパティ[`ApplicationRequest`](/servers/calls/requests.html)
と[`ApplicationResponse`](/servers/calls/responses.html)へのアクセスを提供します。
名前が示しているように、これらはリクエストとレスポンスに対応します。
これらに加えて、`ApplicationEnvironment`とクライアントリクエストへのレスポンスに役立ついくつかの便利な機能も提供します。
パイプラインを非同期で実行できることを前提に、`ApplicationCall`は、パイプラインのさまざまな部分間でデータを渡すための`Attributes`を持つ論理実行コンテキストも表します。

パイプラインへのインターセプターのインストールは、`ApplicationCall`の処理を変更する主要な方法です。ほとんどすべてのKtorの[Feature](/servers/features)は、アプリケーションコール処理のさまざまなフェーズでさまざまな操作を実行するインターセプターです。

```kotlin
    intercept(ApplicationCallPipeline.Call) { 
        if (call.request.uri == "/")
            call.respondText("Test String")
    }
```

上記のコードは、インターセプターを`ApplicationCall`の処理の`Call`フェーズにインストールし、リクエストがルートページを要求しているときにプレーンテキストで応答します。

これは単なる例であり、通常、ページリクエストはこの方法で処理されません。これを行う[routing](/servers/features/routing)機能があるためです。
また、前述のように、インターセプターの定義は通常、インストール機能を備えた[feature](/servers/features)を使用して行われます。

ApplicationCallで使用できるほとんどの関数（上記のrespondTextなど）は`suspend`関数であり、潜在的に非同期で実行できることを示しています。

ApplicationCallsの処理のメカニズムの詳細については、高度なトピック[Pipeline](/advanced/pipeline)を参照してください。

## 関連項目

- [Applicationライフサイクル](https://jp.ktor.work/servers/lifecycle.html)
- [Application設定](https://jp.ktor.work/servers/configuration.html)
- [Pipeline](/advanced/pipeline)
