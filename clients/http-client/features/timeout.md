---
title: タイムアウト
category: clients
caption: タイムアウト
feature:
  artifact: io.ktor:ktor-client-core:$ktor_version
  class: io.ktor.client.features.HttpTimeout
ktor_version_review: 1.4.0
---

リクエスト実行時間、コネクション確率までの時間、および TCP パケットの間隔の上限値 (タイムアウト) を設定することができます。
下記のタイムアウトを設定することができます。

* **request timeout** — リクエスト処理全体の時間制限
* **connect timeout** — 接続確立までの時間制限
* **socket timeout** — 後続の2パケットの間隔の時間制限 (読み取り / 書き込みのタイムアウト)

デフォルトでは、上記のタイムアウトはすべて無限となっているため、必要に応じて明示的に設定する必要があります。
タイムアウト値は、特定のクライアントを用いた際のすべてのリクエストに対して設定することも、特定のリクエストに対して設定することもできます。

{% include feature.html %}

## インストール

特定のクライアントを用いた際のすべてのリクエストに対するリクエストタイムアウト (request timeout) 、コネクションタイムアウト (connect timeout) 、ソケットタイムアウト (socket timeout) の設定は、 `HttpTimeout` をインストールする際に行います。

``` kotlin
val client = HttpClient() {
    install(HttpTimeout) {
        // タイムアウト設定
        requestTimeoutMillis = 1000
    }
}
```

クライアント全体で設定した場合でも、特定のクエリで設定を上書きすることができます。
より大きな値を設定することも可能です。

``` kotlin
val client = HttpClient() {
    install(HttpTimeout)
}
client.get<String>(url) {
    timeout {
        // タイムアウト設定
        requestTimeoutMillis = 5000
    }
}
```

リクエストごとにタイムアウトを設定する場合でも、タイムアウト機能のインストールが必要です。
インストールされていないにも関わらずタイムアウト値を設定した場合、 *"Consider install HttpTimeout feature because request requires it to be installed"* (リクエストが `HttpTimeout` の機能を要求しているため、 `HttpTimeout` のインストールを検討してください) というメッセージと共に `IllegalArgumentException` が発生します。
{: .note}

タイムアウトした場合、例外が発生します。
例外の型は、発生したタイムアウトの種類によって異なります。

* `HttpRequestTimeoutException` : リクエストタイムアウト
* `HttpConnectTimeoutException` : コネクションタイムアウト
* `HttpSocketTimeoutException` : ソケットタイムアウト

## プラットフォーム固有の挙動

すべてのクライアントエンジンがすべてのタイムアウトの種類をサポートしているわけではありません。

* `Curl` : リクエストタイムアウトとコネクションタイムアウトのみサポート
* `Ios` : リクエストタイムアウトとソケットタイムアウトのみサポート
* `Js` : リクエストタイムアウトのみサポート
