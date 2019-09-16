---
title: リダイレクト
category: clients
caption: リダイレクト
feature:
  artifact: io.ktor:ktor-client-core:$ktor_version
  class: io.ktor.client.features.HttpRedirect
ktor_version_review: 1.2.0
---

デフォルトでは、Ktor HTTPクライアントはリダイレクトに従います。
この機能により、任意のHTTPエンジンで機能する方法で`Location`リダイレクトに従うことができます。
その使用方法は非常に簡単であり、構成可能なプロパティはmaxJumps（デフォルトでは20）のみです。
これは（無限リダイレクトを防ぐために）リダイレクトをやめる前のリダイレクトの試行回数を制限します。

{% include feature.html %}

## インストール

このFeatureはデフォルトでインストールされています。

## インストールしない方法

```kotlin
val client = HttpClient(HttpClientEngine) {
    followRedirects = false
}
```

このFeatureはHttpClientのCoreに含まれているため、クライアントとともに常に利用可能です。
{: .note}
