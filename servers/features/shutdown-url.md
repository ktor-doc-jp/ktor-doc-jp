---
title: シャットダウンURL
caption: サーバシャットダウン用のURLの追加
category: servers
permalink: /servers/features/shutdown-url.html
feature:
  artifact: io.ktor:ktor-server-host-common:$ktor_version
  class: io.ktor.server.engine.ShutDownUrl.ApplicationCallFeature
redirect_from:
- /features/shutdown-url.html
ktor_version_review: 1.0.0
---

この機能は、アクセスした際にサーバをシャットダウンさせるURLを有効にする機能です。

[HOCONを使って自動設定する方法](#hocon)と[Featureをインストールする方法](#install)の2つの方法があります。

{% include feature.html %}

## HOCONを使って自動設定
{: #hocon}

HOCONファイルの[ktor.deployment.shutdown.url](/servers/configuration.html#general)プロパティを使って、
シャットダウンURLを設定することができます。

```kotlin
ktor {
    deployment {
        shutdown.url = "/my/shutdown/path"
    }
}
```

## 機能のインストール
{: #install}

`ShutDownUrl.ApplicationCallFeature`を使い、`shutDownUrl`と`exitCodeSupplier`をセットすることで、
Featureを手動でインストールすることができます。

```kotlin
install(ShutDownUrl.ApplicationCallFeature) {
    // The URL that will be intercepted
    shutDownUrl = "/ktor/application/shutdown"
    // A function that will be executed to get the exit code of the process
    exitCodeSupplier = { 0 } // ApplicationCall.() -> Int
}
```