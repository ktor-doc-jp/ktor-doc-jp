---
title: クライアント
caption: クライアントの設定
category: clients
permalink: /clients/http-client/quick-start/client.html
redirect_from:
- /clients/http-client/calls/client.html
ktor_version_review: 1.3.0
---

## エンジンの選定と依存ライブラリの追加

クライアントを使用するためには、まずエンジンの選定と依存ライブラリの追加を行う必要があります。
Ktor はクライアント API を提供しており、実際の HTTP リクエスト処理はエンジンに移譲します。

各プラットフォームごとに、すぐに利用できるエンジンが多数用意されています。

* [`Apache`](/clients/http-client/engines.html#apache)
* [`OkHttp`](/clients/http-client/engines.html#okhttp)
* [`Android`](/clients/http-client/engines.html#android)
* [`Ios`](/clients/http-client/engines.html#ios)
* [`Js`](/clients/http-client/engines.html#js-javascript)
* [`Jetty`](/clients/http-client/engines.html#jetty)
* [`CIO`](/clients/http-client/engines.html#cio)
* [`Mock`](/clients/http-client/testing.html)

[マルチプラットフォーム](/clients/http-client/multiplatform.html) の章により詳しく記載しています。

例えば、 `CIO` を使用する場合は下記を `build.gradle` に記載します。

```kotlin
dependencies {
    implementation("io.ktor:ktor-client-cio:$ktor_version")
}
```

## クライアントの作成

クライアントを作成するには、下記のように書きます。

```kotlin
val client = HttpClient(CIO)
```

この `CIO` はエンジンクラスです。
どのエンジンクラスを使用すべきか迷った場合は、 `CIO` の使用を検討してください。

マルチプラットフォームの場合は、下記のようにエンジンの指定を省略することができます。

```kotlin
val client = HttpClient()
```

JVM ならば `ServiceLoader` を使用し、それ以外のプラットフォームでも同様のアプローチによって、 Ktor はアーティファクト内の利用可能なエンジンを自動的に選択し使用します。
依存ライブラリ内に複数のエンジンがある場合は、アルファベット順に解決します。

クライアントのインスタンスを複数作成しても、同一のクライアントを用いて複数のリクエストをしても、どちらでも問題ありません。

## リソースの開放

Ktor クライアントは予めスレッド、 coroutine 、およびコネクションを確保します。
クライアントを利用し終わった後は、 `close` を呼びリソースを開放したくなると思います。

```kotlin
client.close()
```

1 リクエストごとにクライアントを生成する場合は、 `use` を使用することを検討してください。
use ブロックを抜けた際に、自動的にリソースを開放します。

```kotlin
val status = HttpClient().use { client ->
    ...
}
```

`close` メソッドは、新たなリクエストを停止するよう通知します。
これは非同期に実行され、現在実行中のリクエストが正常終了してからリソースを開放することができます。

また、 `join` メソッドですべてのリクエストが完了することを待ったり、 `cancel` メソッドでリクエストを停止することができます。


```kotlin
try {
    // close 後最大3秒待機
    withTimeout(3000) {
        client.close()
        client.join()
    }
} catch (timeout: TimeoutCancellationException) {
    // タイムアウト後に中止
    client.cancel()
}
```

Ktor の `HttpClient` は `CoroutineScope` のライフサイクルに従います。
詳しくは [Coroutines guide](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-scope/) を参照してください。

## クライアントの設定

クライアントのコンストラクタの関数パラメータでクライアントの設定を行うことができます。
クライアントは [HttpClientEngineConfig](https://api.ktor.io/{{ site.ktor_version }}/io.ktor.client.engine/-http-client-engine-config/index.html) で設定されます。

例えば `threadCount` や [プロキシ](/clients/http-client/features/proxy.html) の設定は下記のように行います。

```kotlin
val client = HttpClient(CIO) {
    threadCount = 2
}
```

エンジン自体の設定を行う場合は、ブロック内で `engine` メソッドを呼びます。

```kotlin
val client = HttpClient(CIO) {
    engine {
        // エンジンの設定
    }
}
```

詳細は [エンジン](/clients/http-client/engines.html) の章に記載されています。

次 : [リクエストの準備](/clients/http-client/quick-start/requests.html).