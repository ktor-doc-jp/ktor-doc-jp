---
title: Callロギング
caption: クライアントのリクエストをログ出力
category: servers
permalink: /servers/features/call-logging.html
feature:
  artifact: io.ktor:ktor-server-core:$ktor_version
  class: io.ktor.features.CallLogging
redirect_from:
- /features/call-logging.html
ktor_version_review: 1.0.0
---

クライアントリクエストをログ出力したいことがあるかもしれません。CallLogging Featureはそれを実現してくれます。
Featureはこれはslf4jを使った`ApplicationEnvironment.log` (`LoggerFactory.getLogger("Application")`)を利用しており、
簡単に出力内容を設定することができます。
Ktorでのロギングについてのより詳細な情報は、[ktor内でのロギング](/servers/logging.html)ページをご覧ください。

{% include feature.html %}

## 基本的な使い方

Featureを未設定の場合、すべてのリクエストに対しTRACEレベルでログ出力します:

```kotlin
install(CallLogging)
```

## 設定

このFeatureはログレベルとログ出力が行われるリクエストのフィルタリングの設定ができます:

```kotlin
install(CallLogging) {
    level = Level.INFO
    filter { call -> call.request.path().startsWith("/section1") }
    filter { call -> call.request.path().startsWith("/section2") }
    // ...
}
```

filterメソッドはホワイトリストとしてフィルター一覧を保持しています。
もしフィルターが定義されていないなら、すべてがログ出力されます。
もしフィルターがあるなら、フィルターのうちいずれかがtrueをreturnする場合にログ出力されます。

この例では、`/section1/*`と`/section2/*`リクエストの両方で出力されます。

## MDC
{: #mdc }

`CallLogging` Featureはslf4jの`MDC` (Mapped Diagnostic Context)をサポートしており、リクエストの一部として情報を紐付けられます。

CallLoggingをインスタンス化するとき、`mdc`メソッドを使ってリクエストに紐付けるパラメータを設定できます。
このメソッドはkey名と関数のProviderを必要とします。
コンテキストは`Monitoring`パイプラインフェーズの一部として紐付けられ（そして関数provider群が呼び出され）ます。

```kotlin
install(CallLogging) {
    mdc(name) { // call: ApplicationCall -> 
        "value"
    }
    // ...
}
```

MDCはThreadLocalsを使って動作しており、Ktorは指定したThreadに紐付かないcoroutinesを利用しています。
このFeatureはその問題に対処するため内部的に`kotlinx.coroutines` [ThreadContextElement](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/-thread-context-element/index.html){: target="_blank"}を利用しています。
{: .note }
