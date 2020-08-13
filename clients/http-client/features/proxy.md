---
title: プロキシ
category: clients
caption: プロキシ
feature:
  artifact: io.ktor:ktor-client-core:$ktor_version
  class: io.ktor.client.engine.ProxyConfig
ktor_version_review: 1.3.0
---

Ktor HTTP クライアントでは、マルチプラットフォーム用のコードでプロキシを使用することができます。
以下のドキュメントでは、 Ktor でプロキシを設定する方法について説明します。

## マルチプラットフォームでの構成

### プロキシの作成

プロキシを作成するために、依存ライブラリの追加をする必要はありません。
サポートされるプロキシの種類は、クライアントエンジンに依存します。
マルチプラットフォーム向けには、2種類のプロキシを構成できます

* [HTTP](https://ja.wikipedia.org/wiki/%E3%83%97%E3%83%AD%E3%82%AD%E3%82%B7)
* [SOCKS](https://ja.wikipedia.org/wiki/SOCKS)

プロキシ構成を作成するには、 [ProxyBuilder]({{ site.api_ktor_io }}/io.ktor.client.engines/-proxy-builder/) を使用します。
p
```kotlin
// HTTP プロキシの作成
val httpProxy = ProxyBuilder.http("http://my-proxy-server-url.com/")

// SOCKS プロキシの作成
val socksProxy = ProxyBuilder.socks(host = "127.0.0.1", port = 4001)
```

プロキシ認証と承認はエンジン固有であり、ユーザが手動で処理する必要があります。

### プロキシの設定

マルチプラットフォーム用のコードでは、 [HttpClientEngineConfig]({{ site.api_ktor_io }}/io.ktor.client.engine/-http-client-engine-config/) 内の [ProxyConfig]({{ site.api_ktor_io }}/io.ktor.client.engines/-proxy-config/) ビルダを用いてプロキシの設定をすることができます。

```kotlin
val client = HttpClient() {
    engine {
        proxy = httpProxy
    }
}
```

## プラットフォーム固有の設定

### Jvm

[ProxyConfig]({{site.api_ktor_io}}/io.ktor.client.engines/-proxy-config/) クラスは JVM の [Proxy](https://docs.oracle.com/javase/7/docs/api/java/net/Proxy.html) クラスにマッピングされます。

```kotlin
val httpProxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(4040))
```

多くの JVM 用クライアントエンジンでは、そのまま使用することができます。

[Apache]({{site.api_ktor_io}}/io.ktor.client.engine.apache/-apache/) エンジンと [CIO]({{site.api_ktor_io}}/io.ktor.client.engine.cio/-c-i-o/) エンジンは HTTP プロキシのみ対応しています。
[Jetty]({{site.api_ktor_io}}/io.ktor.client.engine.jetty/-jetty/) エンジンはプロキシに対応していません。
{: .note }

### Native

ネイティブの [ProxyConfig]({{ site.api_ktor_io }}/io.ktor.client.engines/-proxy-config/) クラスは URL を使用してプロキシアドレスを指定できます。

```kotlin
val socksProxy = ProxyConfig(url = "socks://my-socks-proxy.com/")
```

サポートされるプロキシの種類はエンジンに依存します。
サポートされている URL を確認するには、エンジン自身が提供しているドキュメントを参照してください。

- Curl: [https://curl.haxx.se/libcurl/c/CURLOPT_PROXY.html](https://curl.haxx.se/libcurl/c/CURLOPT_PROXY.html)
- iOS: [https://developer.apple.com/documentation/foundation/nsurlsessionconfiguration/1411499-connectionproxydictionary](https://developer.apple.com/documentation/foundation/nsurlsessionconfiguration/1411499-connectionproxydictionary)

### Js

プロキシの構成は、プラットフォームの制約上サポートされていません。
