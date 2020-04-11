---
title: Httpクライアント
category: clients
permalink: /clients/index.html
children: /clients/http-client/
caption: Httpクライアント
ktor_version_review: 1.2.3
---

{::options toc_levels="1..2" /}

HTTPによる配信機能に加え、Ktorは非同期で柔軟なHTTPクライアント機能も含んでいます。
このクライアント機能はいくつかの[設定可能なengine](/clients/http-client/engines.html)をサポートしています。
またいくつかの[Feature](/clients/http-client/features.html)もサポートしています。

メイン機能は`io.ktor:ktor-client-core:$ktor_version`アーティファクトを通じて利用可能です。
各engineは、[アーティファクトとして分離された上で](/clients/http-client/engines.html)提供されています。
{: .note.artifact }

**目次:**

* TOC
{:toc}

## Call: リクエストとレスポンス

{: #requests-responses }

[リクエストの作り方](/clients/http-client/quick-start/requests.html)を確認してください。
また、[レスポンスの受け取り方](/clients/http-client/quick-start/responses.html)も確認してください。

## 並行性

リクエストは非同期ですが、リクエスト実行時にAPIは別の追加のリクエストを行わず、実行終了するまで関数は停止することを覚えておいてください。
いくつかのリクエストを同じブロック内で一度に実行したい場合は、`launch`か`async`関数を使い結果を遅延で受け取れば良いです。
例えば:

### シーケンシャルなリクエスト

```kotlin
suspend fun sequentialRequests() {
    val client = HttpClient()

    // Get the content of an URL.
    val firstBytes = client.get<ByteArray>("https://127.0.0.1:8080/a")

    // Once the previous request is done, get the content of an URL.
    val secondBytes = client.get<ByteArray>("https://127.0.0.1:8080/b")

    client.close()
}
```

### 並列リクエスト

```kotlin
suspend fun parallelRequests() = coroutineScope<Unit> {
    val client = HttpClient()

    // Start two requests asynchronously.
    val firstRequest = async { client.get<ByteArray>("https://127.0.0.1:8080/a") }
    val secondRequest = async { client.get<ByteArray>("https://127.0.0.1:8080/b") }

    // Get the request contents without blocking threads, but suspending the function until both
    // requests are done.
    val bytes1 = firstRequest.await() // Suspension point.
    val bytes2 = secondRequest.await() // Suspension point.

    client.close()
}
```

## サンプル
{: #examples }

[サンプルページ](/clients/http-client/examples.html)にいくつかの例があります。

## Features
{: #features}

[Featureページ](/clients/http-client/features.html)に利用可能なすべての機能があります。
