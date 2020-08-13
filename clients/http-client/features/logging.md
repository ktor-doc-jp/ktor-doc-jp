---
title: ロギング
category: clients
caption: ロギング
feature:
  artifact: io.ktor:ktor-client-logging:$ktor_version
  class: io.ktor.client.features.logging.Logging
ktor_version_review: 1.2.0
---

マルチプラットフォーム対応済の、HTTP リクエスト時のログ出力処理機能を追加できます。

{% include 
    mpp_feature.html
    targets="common,jvm,native,js"
    base="ktor-client-logging"
    classifiers=",-jvm,-native,-js"
%}

## インストール

```kotlin
val client = HttpClient() {
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.HEADERS
    }
}
```

この機能を使う場合、 JVM ならば `io.ktor:ktor-client-logging-jvm` 、 iOS ならば `ktor-client-logging-native` を依存ライブラリとして追加する必要があります。
{: .note.artifact }
