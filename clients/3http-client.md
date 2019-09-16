---
title: Httpクライアント
category: clients
permalink: /clients/http-client.html
children: /clients/http-client/
caption: Httpクライアント
ktor_version_review: 1.2.0
---

{::options toc_levels="1..2" /}

HTTPサーバーに加えて、Ktorには柔軟な非同期HTTPクライアントも含まれています。
このクライアントは、いくつかの構成可能なエンジンをサポートし、独自の[機能](/clients/http-client/features.html)セットを備えています。

主な機能は、`io.ktor:ktor-client-core:$ktor_version`アーティファクトを介して利用できます。 また、各エンジンは、[個別の成果物](/clients/http-client/engines.html)で提供されます。
{: .note.artifact }

**Table of contents:**

* TOC
{:toc}

## Calls: Requests and Responses

{: #requests-responses }

それぞれのセクションで[リクエストの作成方法](/clients/http-client/call/requests.html)と[レスポンスの受信方法](/clients/http-client/call/responses.html)を確認できます。

## 並列処理

リクエストは非同期ですが、リクエストを実行すると、APIはさらにリクエストを一時停止し、機能は完了するまで一時停止されます。
同じブロックで複数の要求を一度に実行する場合は、`launch`または`async`関数を使用して、後で結果を取得できます。 例えば：

### Sequential requests

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

### Parallel requests

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

詳細な情報は[サンプルページ](/clients/http-client/examples.html)をご覧ください。

## Feature
{: #features}

詳細な情報は[機能ページ](/clients/http-client/features.html)をご覧ください。
