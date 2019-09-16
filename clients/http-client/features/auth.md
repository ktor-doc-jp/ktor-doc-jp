---
title: 認証
category: clients
caption: 認証
feature:
  artifact: io.ktor:ktor-client-auth:$ktor_version
  class: io.ktor.client.features.auth.Auth
keywords: authentication
redirect_from:
- /clients/http-client/features/basic-auth.html
ktor_version_review: 1.2.0
---

この文章は機械翻訳を利用して翻訳されています。翻訳してくださる有志の方をお待ちしております。
{: .note.machine-translation}

Ktorクライアントは、標準のプラグイン可能な機能として、すぐに使用できる認証をサポートしています。

{% include feature.html %}

## Installation

``` kotlin
val client = HttpClient() {
    install(Auth) {
        // providers config
        ...
    }
}
```

## Providers

### Basic

このProviderは`Authorization: Basic`を特定のクレデンシャルとともに送信します。

```kotlin
val client = HttpClient() {
    install(Auth) {
        basic {
            username = "username"
            password = "password"
        }
    }
}
```

この機能はIETFの[RFC 7617](https://tools.ietf.org/html/rfc7617)を実装しています。

### Digest

このProviderは`Authorization: Digest`を特定のクレデンシャルとともに送信します。

```kotlin
val client = HttpClient() {
    install(Auth) {
        digest {
            username = "username"
            password = "password"
            realm = "custom"
        }
    }
}
```

この機能はIETFの[RFC 2617](https://tools.ietf.org/html/rfc2617)を実装しています。
