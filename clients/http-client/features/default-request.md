---
title: デフォルトリクエスト
category: clients
caption: デフォルトリクエスト
feature:
  artifact: io.ktor:ktor-client-core:$ktor_version
  class: io.ktor.client.features.DefaultRequest
  method: io.ktor.client.features.defaultRequest
ktor_version_review: 1.2.0
---

この文章は機械翻訳を利用して翻訳されています。翻訳してくださる有志の方をお待ちしております。
{: .note.machine-translation}

この機能を使用すると、特定のクライアントのすべての要求に対していくつかのデフォルトを設定できます。

{% include feature.html %}

## インストール

クライアントを設定するとき、このクライアントにデフォルトを設定するために、この機能によって提供される拡張メソッドがあります。
たとえば、すべての要求にヘッダーを追加する場合、ホスト、ポート、およびメソッドを構成する場合、または単にパスを設定する場合です。

```kotlin
val client = HttpClient() {
    defaultRequest { // this: HttpRequestBuilder ->
        method = HttpMethod.Head
        host = "127.0.0.1"
        port = 8080
        header("X-My-Header", "MyValue")
    }
}
```

## サンプル

以下の例はクライアントが[MockEngine](/clients/http-client/testing.html)を利用した際にどのように振る舞うかを示しています。

```kotlin
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.io.*

fun main(args: Array<String>) = runBlocking {
    val client = HttpClient(MockEngine) {
        engine {
            // Register request handler.
            addHandler { request ->
                with(request) {
                    val responseText = buildString{
                        append("method=$method,")
                        append("host=${url.host},")
                        append("port=${url.port},")
                        append("path=${url.fullPath},")
                        append("headers=$headers")
                    }
                    val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Text.Plain.toString()))

                    respond(responseText, headers = responseHeaders)
                }
            }
        }

        // Configure default request feature.
        defaultRequest {
            method = HttpMethod.Head
            host = "127.0.0.1"
            port = 8080
            header("X-My-Header", "MyValue")
        }
    }

    val result = client.get<String> {
        url {
            encodedPath = "/demo"
        }
    }

    println(result)
    // Prints: method=HttpMethod(value=HEAD), host=127.0.0.1, port=8080, path=/demo, headers=Headers [X-My-Header=[MyValue], Accept=[*/*]]
}

```
