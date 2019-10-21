---
title: Dropwizard metrics を用いたメトリクスの収集
caption: Dropwizard metrics を用いたメトリクスの収集
category: servers
permalink: /servers/features/metrics.html
feature:
  artifact: io.ktor:ktor-metrics:$ktor_version
  class: io.ktor.metrics.DropwizardMetrics
redirect_from:
- /features/metrics.html
ktor_version_review: 1.0.0
---

Metrics feature を用いると [Metrics](http://metrics.dropwizard.io/4.0.0/) を構成してサーバとリクエストに関する 情報を取得できます。

{% include feature.html %}

## インストール

Metrics feature はメトリクスレポーターの構築と送出のための `registry` プロパティを生成します。

### JMX Reporter

JMX Reporter はすべてのメトリクスを JMX に公開し、 `jconsole` や MBeans plugin を適用した `jvisualvm` で
メトリクスの閲覧ができるようになります。

```kotlin
install(DropwizardMetrics) {
    JmxReporter.forRegistry(registry)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build()
        .start()
}
```

![Ktor Metrics: JMX](/servers/features/metrics/jmx.png)

### SLF4J Reporter

SLF4J Reporter を使用すると、 SLF4J でサポートしている形式のレポートを定期的に送出できるようになります。
例えば、10秒ごとにメトリクスを出力したい場合は下記のように設定します。

```kotlin
install(DropwizardMetrics) {
    Slf4jReporter.forRegistry(registry)
        .outputTo(log)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build()
        .start(10, TimeUnit.SECONDS)
}
```

![Ktor Metrics: SLF4J](/servers/features/metrics/slf4j.png)

### その他のレポーター

利用可能なメトリクスレポーターの一覧は [Metric reporters](http://metrics.dropwizard.io/4.0.0/) に記載されています。

## Exposed reports

This feature exposes many JVM properties relating to memory usage and thread behavior.

この feature はメモリの使用量やスレッドの動作等、多くの JVM のメトリクスを送出します。

### 全体:

Ktor 全体として、下記のものを送出します。

* `ktor.calls.active`:`Count` - 処理中のリクエスト数
* `ktor.calls.duration` - レスポンスタイム
* `ktor.calls.exceptions` - 例外の発生件数
* `ktor.calls.status.NNN` - 各 HTTP ステータスコードごとの件数

### Per endpoint:

* `"/uri(method:VERB).NNN"` - 各リクエストパス、HTTP メソッド、HTTP ステータスコードごとの件数
* `"/uri(method:VERB).meter"` - 各リクエストパス、HTTP メソッドごとの件数
* `"/uri(method:VERB).timer"` - 各エンドポイントごとの処理時間

## レポートごとの情報

### 期間

`"/uri(method:VERB).timer"` と `ktor.calls.duration` は下記のものを集計できます。

* 50thPercentile
* 75thPercentile
* 95thPercentile
* 98thPercentile
* 99thPercentile
* 999thPercentile
* Count
* DurationUnit
* OneMinuteRate
* FifteenMinuteRate
* FiveMinuteRate
* Max
* Mean
* MeanRate
* Min
* RateUnit
* StdDev

### 件数

他のプロパティは件数を集計します。

* Count
* FifteenMinuteRate
* FiveMinuteRate
* OneMinuteRate
* MeanRate
* RateUnit
