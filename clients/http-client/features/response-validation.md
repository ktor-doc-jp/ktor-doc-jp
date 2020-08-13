---
title: レスポンスの検証
category: clients
caption: レスポンスの検証
feature:
  artifact: io.ktor:ktor-client-core:$ktor_version
  class: io.ktor.client.features.HttpCallValidator
redirect_from:
- /clients/http-client/features/expect-success.html
ktor_version_review: 1.2.0
---

HTTP レスポンスを検証し、エンジンやパイプラインからの変換例外を捕捉する機能を提供しています。

{% include feature.html %}

## 設定

レスポンス検証機能の設定では、 `validateResponse` と `handleResponseException` メソッドを使用します。

```kotlin
HttpResponseValidator {
    validateResponse { response: HttpResponse ->
        // ...
    }

    handleResponseException { cause: Throwable ->
        // ...
    }
}
```

この機能は複数個設定することができます。
すべてのバリデータとハンドラは保持され、定義された順番で呼び出されます。

## 成功の期待

`ExpectSuccess` 機能は、レスポンスのバリデーションを利用して実装されます。

```kotlin
HttpResponseValidator {
    validateResponse { response ->
        val statusCode = response.status.value
        when (statusCode) {
            in 300..399 -> throw RedirectResponseException(response)
            in 400..499 -> throw ClientRequestException(response)
            in 500..599 -> throw ServerResponseException(response)
        }

        if (statusCode >= 600) {
            throw ResponseException(response)
        }
    }
}
```

この機能はデフォルトでインストールされていますが、クライアントの設定で無効化することができます。

```kotlin
val client = HttpClient() {
    expectSuccess = false
}
```
