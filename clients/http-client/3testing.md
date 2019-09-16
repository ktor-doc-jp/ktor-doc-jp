---
title: テスト
category: clients
permalink: /clients/http-client/testing.html
caption: Httpクライアントのテスト(モック)
ktor_version_review: 1.2.0
---

Ktorは、HttpClientの`MockEngine`を公開します。
このエンジンにより、実際にエンドポイントに接続せずにHTTP呼び出しをシミュレートできます。
リクエストを処理してレスポンスを生成できるコードブロックを設定できます。

{% include artifact.html kind="engine" class="io.ktor.client.engine.mock.MockEngine" artifact="io.ktor:ktor-client-mock:$ktor_version,io.ktor:ktor-client-mock-jvm:$ktor_version,io.ktor:ktor-client-mock-js:$ktor_version,io.ktor:ktor-client-mock-native:$ktor_version" test="true" %}

## 使い方

使い方は簡単です。MockEngineクラスには、`MockEngineConfig`に`addHandler`メソッドがあり、リクエストを処理するブロック/コールバックを受け取ります。
このコールバックは、`HttpRequest`をパラメーターとして受け取り、`HttpResponseData`を返す必要があります。レスポンスを作成する多くのヘルパーメソッドがあります。

完全なAPIの説明とヘルパーメソッドのリストは、[こちら](https://api.ktor.io/{{site.ktor_version}}/io.ktor.client.engine.mock/)にあります。

以下がサンプルです：

```kotlin
val client = HttpClient(MockEngine) {
    engine {
        addHandler { request ->
            when (request.url.fullUrl) {
                "https://example.org/" -> {
                    val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Text.Plain.toString()))
                    respond("Hello, world", headers = responseHeaders)
                }
                else -> error("Unhandled ${request.url.fullUrl}")
            }
        }
    }
}

private val Url.hostWithPortIfRequired: String get() = if (port == protocol.defaultPort) host else hostWithPort
private val Url.fullUrl: String get() = "${protocol.name}://$hostWithPortIfRequired$fullPath"
```
