---
title: エンジン
category: clients
permalink: /clients/http-client/engines.html
caption: HTTP クライアントエンジン
ktor_version_review: 1.2.0
---

Ktor HTTP クライアントは共通のインタフェースであり、ネットワークリクエストを処理するためのエンジンを自由に指定することができます。
エンジンが異なれば、構成、依存関係、サポートされる機能も異なります。

**目次**

* TOC
{:toc}

## デフォルト エンジン

{: #default}

エンジン の指定なしで `HttpClient` を生成した場合、デフォルトのエンジンを使用します。

```kotlin
val client = HttpClient()
```

JVM の場合、 `ServiceLoader` を用いてデフォルトのエンジンを解決します。
複数のエンジンが利用可能な場合は、アルファベット順で最初にヒットしたものを使用します。
したがって、依存関係に追加したものによって異なります。

Native の場合、静的リンク時に検出されたものが利用されます。
成果物に native 用のエンジンを1つ含めるようにしてください。

JavaScript の場合、予め定義されたものを1つ使用します。

## エンジン の設定

{: #configuring}

Ktor `HttpClient` では、以下を呼び出すことで各エンジンのパラメータを設定することができます。

```kotlin
HttpClient(MyHttpEngine) {
    engine {
        // this: MyHttpEngineConfig
    }
}
```

すべてのエンジンで共通のプロパティがあり、それを設定することができます。

* `threadsCount` プロパティはエンジンが使うスレッド数の推奨値
    * エンジンがスレッド数を要求しない場合、この設定は無視される
* `pipelining` プロパティは [HTTP pipelining](https://en.wikipedia.org/wiki/HTTP_pipelining) を有効化するための試験的なフラグ

```kotlin
val client = HttpClient(MyHttpEngine) {
    engine {
        threadsCount = 4
        pipelining = true
    }
}
```

## JVM

### Apache

{: #apache}

Apache は現時点では最も多くの設定ができる HTTP クライアントです。
HTTP/1.1 と HTTP/2 に対応しています。
以下のようなリダイレクトやタイムアウトの設定、さまざまなものへのプロキシ (`org.apache.httpcomponents:httpasyncclient` でサポートされているものの間) の設定が可能な唯一のクライアントです。


サンプルの設定は以下のようになります。

```kotlin
val client = HttpClient(Apache) {
    engine {
        /**
         * Apache 備え付けの http リダイレクト; false がデフォルト値
         * `HttpRedirect` の機能により廃止された
         * Apache の HttpClient にて定義されているデフォルトのリダイレクト数 50 を使用する
         */
        followRedirects = true

        /**
         * タイムアウト
         * 0 は無限 (タイムアウトなし)
         * 負の数の場合はシステムのデフォルト値を使用する
         */

        /**
         * TCP ソケット通信のタイムアウト値
         * デフォルトは 10 秒
         */
        socketTimeout = 10_000

        /**
         * HTTP コネクションを確立するまでのタイムアウト値
         * デフォルトは 10 秒
         */
        connectTimeout = 10_000

        /**
         * コネクションマネージャがリクエストを開始するまでのタイムアウト値
         * デフォルトは 20 秒
         */
        connectionRequestTimeout = 20_000

        customizeClient {
            // this: HttpAsyncClientBuilder
            setProxy(HttpHost("127.0.0.1", 8080))

            // ソケット通信のコネクション数の最大値
            setMaxConnTotal(1000)

            // 特定のエンドポイントへのリクエスト数の最大値
            setMaxConnPerRoute(100)

            // ...
        }
        customizeRequest {
            // this: RequestConfig.Builder from Apache.
        }
    }
}
```

{: .compact}

{% include artifact.html kind="engine" class="io.ktor.client.engine.apache.Apache" artifact="io.ktor:ktor-client-apache:$ktor_version" transitive="org.apache.httpcomponents:httpasyncclient" %}

### CIO

{: #cio}

CIO (Coroutine-based I/O) は Ktor によるエンジンで、依存ライブラリの追加が不要、そして非同期処理に対応しています。
HTTP/1.x のみサポートしています。

CIO では `maxConnectionsCount` と `endpointConfig` の設定が可能です。

設定例

```kotlin
val client = HttpClient(CIO) {
    engine {
        /**
         * 最大コネクション数
         */
        maxConnectionsCount = 1000

        /**
         * エンドポイント固有の設定
         */
        endpoint {
            /**
             * 特定のエンドポイントに対するリクエストの最大数
             */
            maxConnectionsPerRoute = 100

            /**
             * コネクションごとのスケジュール済リクエストの最大数 (パイプラインのキューのサイズ)
             */
            pipelineMaxSize = 20

            /**
             * 接続のアイドル状態 (keep-alive) を維持する最大時間 (ミリ秒)
             */
            keepAliveTime = 5000

            /**
             * サーバへの接続のタイムアウト値 (ミリ秒)
             */
            connectTimeout = 5000

            /**
             * 接続ごとのリトライの最大値
             */
            connectRetryAttempts = 5
        }

        /**
         * HTTPS 用の設定
         */
        https {
            /**
             * SSL/TLS 用のホスト名 (SNI)
             * cf. https://ja.wikipedia.org/wiki/Server_Name_Indication
             */
            serverName = "api.ktor.io"

            /**
             * 許可する暗号スイート (Cipher suite)
             */
            cipherSuites = CIOCipherSuites.SupportedSuites

            /**
             * サーバ認証を検証する X509TrustManager 型のインスタンス
             *
             * デフォルトでは system を利用
             */
            trustManager = myCustomTrustManager

            /**
             * 暗号化の際に用いられる SecureRandom 型のインスタンス
             */
            random = mySecureRandom
        }
    }
}
```

{: .compact}

{% include artifact.html kind="engine" class="io.ktor.client.engine.cio.CIO" artifact="io.ktor:ktor-client-cio:$ktor_version" %}

### Jetty

{: #jetty}

Jetty では `sslContextFactory` の設定が可能です。
現時点では、 HTTP/2 のみ対応しています。

設定例

```kotlin
val client = HttpClient(Jetty) {
    engine {
        sslContextFactory = SslContextFactory()
    }
}
```

{% include artifact.html kind="engine" class="io.ktor.client.engine.jetty.Jetty" artifact="io.ktor:ktor-client-jetty:$ktor_version" transitive="org.eclipse.jetty.http2:http2-client" %}

## JVM および Android

### OkHttp

{: #okhttp }

OkHttp ベースのエンジン

```kotlin
val client = HttpClient(OkHttp) {
    engine {
        // https://square.github.io/okhttp/3.x/okhttp/okhttp3/OkHttpClient.Builder.html
        config { // this: OkHttpClient.Builder ->
            // ...
            followRedirects(true)
            // ...
        }

        // https://square.github.io/okhttp/3.x/okhttp/okhttp3/Interceptor.html
        addInterceptor(interceptor)
        addNetworkInterceptor(interceptor)

        /**
         * 自前で用意した OkHttp クライアントのインスタンスを指定
         * 設定しなかった場合、自動的にインスタンスが生成される
         */
        preconfigured = okHttpClientInstance
    }

}
```

{% include artifact.html kind="engine" class="io.ktor.client.engine.okhttp.OkHttp" artifact="io.ktor:ktor-client-okhttp:$ktor_version" transitive="com.squareup.okhttp3:okhttp" %}

### Android

{: #android }

Android エンジンは依存ライブラリの追加が不要で、 `HttpURLConnection` と `ThreadPool` を使用してリクエストを実行します。

設定例

```kotlin
val client = HttpClient(Android) {
    engine {
        connectTimeout = 100_000
        socketTimeout = 100_000

        /**
         * プロキシの設定
         */
        proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress("localhost", serverPort))
    }
}
```

{% include artifact.html kind="engine" class="io.ktor.client.engine.android.Android" artifact="io.ktor:ktor-client-android:$ktor_version" %}

## iOS

{: #ios }

iOS エンジンは非同期の `NSURLSession` を内部で使用します。
追加の設定は不要です。

```kotlin
val client = HttpClient(Ios) {
    /**
     * native の NSUrlRequest の設定
     */
    configureRequest { // this: NSMutableURLRequest
        setAllowsCellularAccess(true)
        // ...
    }
}
```

{% include artifact.html kind="engine" class="io.ktor.client.engine.ios.Ios" artifact="io.ktor:ktor-client-ios:$ktor_version" %}

## Js (JavaScript)

`Js` エンジンは [`fetch`](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API) API を内部的に使用します。
node.js 上では `node-fetch` を使用します。

`Js` エンジンには追加の設定項目はありません。

```kotlin
val client = HttpClient(Js) {
}
```

`JsClient()` 関数を呼ぶことで、 `Js` エンジンのシングルトンインスタンスを取得することもできます。

{% include artifact.html kind="engine" class="io.ktor.client.engine.js.Js" artifact="io.ktor:ktor-client-js:$ktor_version" %}

## Curl

{: #curl }

Curl ベースのエンジン

```kotlin
val client = HttpClient(Curl)
```

サポートされるプラットフォーム : `linux_x64`, `macos_x64`, `mingw_x64`
エンジンを使用するには、 バージョン 7.63 以上の `curl` ライブラリがインストールされている必要があります。


{% include artifact.html kind="engine" class="io.ktor.client.engine.curl.Curl" artifact="io.ktor:ktor-client-curl:$ktor_version" %}

## MockEngine

`MockEngine` はテスト用のエンジンです。
[MockEngine for testing](/clients/http-client/testing.html) を参照してください。
