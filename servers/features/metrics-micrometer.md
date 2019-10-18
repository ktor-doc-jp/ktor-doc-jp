---
title: Micrometer metrics を用いたメトリクス収集
caption: Micrometer metrics を用いたメトリクス収集
category: servers
permalink: /servers/features/metrics-micrometer.html
feature:
  artifact: io.ktor:ktor-metrics-micrometer:$ktor_version
  class: io.ktor.metrics.MicrometerMetrics
redirect_from:
- /features/metrics.html
ktor_version_review: 1.0.0
---

Metrics feature を用いると [Metrics](https://micrometer.io/) を構成してサーバとリクエストに関する
情報を取得できます。
この実装は JRE 8 以降と Micrometer Metrics を使用しています。

{% include feature.html %}

## メトリクスの出力

利用したい時系列データベースによりますが、ここでは [vary](https://micrometer.io/docs/concepts#_naming_meters) の
命名規則に従います。

### `ktor.http.server.requests.active`
アクティブ [gauge](https://micrometer.io/docs/concepts#_gauges) は、
サーバへの同時HTTPリクエスト数をカウントします。
このメトリクスにはタグはありません。

### `ktor.http.server.requests`
[timer](https://micrometer.io/docs/concepts#_timers) は各リクエストのレスポンスタイムを測定します。
この機能は、次のタグを提供します。

- `address`: リクエスト元のクライアントの `<host>:<port>`
- `method`: HTTP メソッド (`GET` や `POST` など)
- `route`: ktor がハンドリングするリクエストパス (`/vehicles/23847/tires/frontright` のパス `/vehicles/{id}/tires/{tire}` など)
- `status`: クライアントへ送出した HTTP ステータスコード (`200` や `404` など)
- `exception`: ハンドラがクライアントへ応答する前に Exception や Throwable をスローした場合、例外の名前または `n/a`  
  クライアントへの応答後にスローされた例外は記録されません。

## インストール
Metrics feature を利用する場合は、利用したい `MeterRegistry` を指定する必要があります。
テスト利用の場合は `SimpleMeterRegistry` を利用します。
より実践的なものを利用する場合は、 [any registry depending on your timeline database vendor](https://micrometer.io/docs) から
選択してください。

```kotlin
install(MicrometerMetrics) {
   registry = SimpleMeterRegistry()
}
```

### Meter Binders

Micromerter は低レイヤのメトリクスも収集します。
このメトリクスは `MeterBinder` から提供されます。
デフォルトではこれらのメトリクスを収集しますが、これらのメトリクスを収集したくない場合や他のメトリクスを収集したい場合は、
`meterBinders` を再定義します。
(メトリクスを収集したくない場合は空リストを指定します)

```kotlin
install(MicrometerMetrics) {
   registry = SimpleMeterRegistry()
   meterBinders = listOf(
            ClassLoaderMetrics(),
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            ProcessorMetrics(),
            JvmThreadMetrics(),
            FileDescriptorMetrics()
   )
}
```

### 統計分布の設定
Micrometer はいろいろなヒストグラムの表現方法を提供します。
クライアントサイドにてパーセンタイルやヒストグラムカウンタで表現できます。
(時系列データベースはサーバサイドでパーセンタイルを計算します。)
パーセンタイルはすべてのバックエンドでサポートされていますが、 [メモリ効率が悪い](https://micrometer.io/docs/concepts#_memory_footprint_estimation)
ため多次元での集計ができません。
ヒストグラムでは対応できますが、全てのバックエンドでサポートされているとは限りません。
詳細は[こちら](https://micrometer.io/docs/concepts#_histograms_and_percentiles)を参照してください。

デフォルトでは、タイマーは50%、90%、95%、99%パーセンタイルを提供します。
`DistributionStatisticConfig` feature をタイマーに適用することで、独自の設定が可能になります。

```kotlin
install(MicrometerMetrics) {
    registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    
    distributionStatisticConfig = DistributionStatisticConfig.Builder()
                .percentilesHistogram(true)
                .maximumExpectedValue(Duration.ofSeconds(20).toNanos())
                .sla(
                    Duration.ofMillis(100).toNanos(),
                    Duration.ofMillis(500).toNanos()
                )
                .build()
}
```

### タイマーのカスタマイズ
各タイマーのタグをカスタマイズするには、各リクエストごとに呼び出されるラムダ関数を作成し、
タイマーのビルダーを用いて適用します。
タグの値の組み合わせごとに独自のメトリクスが生成されることに注意してください。
そのため、カーディナリティの高い値 (リソースIDなど) をタグに指定することは非推奨です。

```kotlin
install(MicrometerMetrics) {
    registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    
    timers { call, exception ->
        this.tag("tenant", call.request.headers["tenantId"])
    }
}
```

