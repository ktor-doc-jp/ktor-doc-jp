---
title: 環境
caption: 環境ごとの差異をどうするか
category: servers
ktor_version_review: 1.0.0
---

あなたのサーバがローカルで起動しているか本番環境のマシンで起動しているかによって、異なることを行いたいことがあるかもしれません。

Ktorはこの機能をフレームワークとしては提供しませんが、どのようにして実現すればいいか迷った際のガイドラインを以下に記載します。

## HOCON & ENV
{: #proposal }

`application.conf`ファイルに、環境を表す変数をセットし、その変数を実行時にチェックしてどういった処理を行うかを動的に決定することができます。
環境変数`KTOR_ENV`を使いデフォルト値として`dev`を与えることで設定することができます。
本番環境では`KTOR_ENV=prod`と設定します。

例えば

### application.conf:

```
ktor {
    environment = dev
    environment = ${?KTOR_ENV}
}
```

この設定ファイルにはアプリケーションからアクセスすることができます。
拡張プロパティ機能を使うことでこれをより簡潔に実装できます。

```kotlin
fun Application.module() {
    when {
        isDev -> {
            // Do things only in dev   
        }
        isProd -> {
            // Do things only in prod
        }
    }
    // Do things for all the environments
}

val Application.envKind get() = environment.config.property("ktor.environment").getString()
val Application.isDev get() = envKind == "dev"
val Application.isProd get() = envKind != "dev"
```
