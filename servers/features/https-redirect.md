---
title: Httpsリダイレクト
caption: HTTPリクエストをHTTPSにリダイレクト
category: servers
permalink: /servers/features/https-redirect.html
keywords: https ssl
feature:
  artifact: io.ktor:ktor-server-core:$ktor_version
  class: io.ktor.features.HttpsRedirect
redirect_from:
- /features/https-redirect.html
ktor_version_review: 1.0.0
---

このFeatureはHTTPコールを、処理を行う前に、対応するHTTPSロケーションへとリダイレクトする機能です。

デフォルトでは、リダイレクトは`301 Moved Permanently`ですが、
`302 Found`リダイレクトに設定することもできます。

{% include feature.html %}

## 使い方

```kotlin
fun Application.main() {
    install(HttpsRedirect)
    // install(XForwardedHeaderSupport) // Required when behind a reverse-proxy
}
```

上のコードはHttpsRedirect Featureをデフォルト設定でインストールします。

reverse-proxy背後にある場合、`ForwardedHeaderSupport`か`XForwardedHeaderSupport` Featureをインストールする必要があります。
`HttpsRedirect` Featureが適切にHTTPSリクエストを検知することができるようにするためです。
{: .note}

## 設定

```kotlin
fun Application.main() {
    install(HttpsRedirect) {
        // The port to redirect to. By default 443, the default HTTPS port. 
        sslPort = 443
        // 301 Moved Permanently, or 302 Found redirect.
        permanentRedirect = true
    }
}
```

## テスト
{: #testing }

このFeatureを適用することで、 [テスティング](/servers/testing.html)の動作が変わります。
このFeatureの適用後、`handleRequest`を実行する度に、リダイレクトレスポンスになります。
例え振る舞いがすでにテスト済みだったとしても、おそらく大抵の場合これは求めている挙動ではないです。

### XForwardedHeaderSupport trick

[このテスト](https://github.com/ktorio/ktor/blob/bb0765ce00e5746c954fea70270cf7d802a40648/ktor-server/ktor-server-tests/test/io/ktor/tests/server/features/HttpsRedirectFeatureTest.kt#L31-L49){: target="_blank"}
で示されているように、
`XForwardedHeaderSupport`をインストールし、`addHeader(HttpHeaders.XForwardedProto, "https")`ヘッダーをリクエストに追加することができます。

```kotlin
@Test
fun testRedirectHttps() {
    withTestApplication {
        application.install(XForwardedHeaderSupport)
        application.install(HttpsRedirect)
        application.routing {
            get("/") {
                call.respond("ok")
            }
        }


        handleRequest(HttpMethod.Get, "/", {
            addHeader(HttpHeaders.XForwardedProto, "https")
        }).let { call ->
            assertEquals(HttpStatusCode.OK, call.response.status())
        }
    }
}
```

### テスト時にFeatureをインストールしないか、あるいはアンインストールする

アンインストール:

```kotlin
application.uninstall(HttpsRedirect)
```

インストールを防ぐ方法:

```kotlin
// The function referenced in the application.conf
fun Application.mymodule() {
    mymoduleConfigured()
}

// The function referenced in the tests
fun Application.mymoduleForTesting() {
    mymoduleConfigured(installHttpsRedirect = false)
}

fun Application.mymoduleConfigured(installHttpsRedirect: Boolean = true) {
    if (installHttpsRedirect) {
        install(HttpsRedirect)
    }
    // ...
}
```

このケースにおいては、`mymoduleForTesting`の代わりに`mymodule`を呼び出す別のテストを作成することもできます。
それによって`HttpsRedirect`機能がインストールされることと、テストにおいて行えないことの検証も行うことができます。

### この機能を使ったとき無限リダイレクトになる場合

`XForwardedHeaderSupport`か`ForwardedHeaderSupport`をインストール済みですか？
[このFAQエントリ](/quickstart/faq.html#infinite-redirect)をチェックしてみてください。
