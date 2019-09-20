---
title: Configuration
caption: サーバーの設定
keywords: hocon json config DevelopmentEngine EngineMain embeddedServer switches cli command line environment variables ports application modules ssl host bind application.conf mainClassName jetty netty tomcat cio
category: servers
permalink: /servers/configuration.html
children: /servers/configuration/
---

Ktorは外部設定ファイルに[HOCON (Human-Optimized Config Object Notation)](https://github.com/lightbend/config/blob/master/HOCON.md)フォーマットを利用しています。
このファイル内ではリッスンするport番号であったり、ロードする[module](/servers/application.html#modules)であったりを設定します。
JSONに似たフォーマットですが、人が読み書きしやすいよう最適化されており、環境変数の置換のような追加機能をサポートしています。
以下のケースでは、`mainClassName`を使うことで特定の`EngineMain`を指定し、サーバーエンジンの設定を行います。

またKtorは型付けされたDSL(Domain Specific Language)とともにlambdaのセットを用い、アプリケーションとサーバーエンジンを`embeddedServer`を使って設定することもできます。

Ktor 1.0.0-beta-2で始める場合は、`DevelopmentEngine`クラスは`EngineMain`にリネームされているため、古いバージョンを使う場合はリネームしてください。
{: .note }

**目次:**

* TOC
{:toc}

## HOCONファイル
{: #hocon-file}

アプリケーションを再コンパイルすることなく設定を簡単に変更できるため、この方法がKtorアプリケーションを設定する上での推奨される方法となります。

Ktorが`EngineMain`を使って起動されたときや`commandLineEnvironment`を呼び出したとき、アプリケーションresourceから`application.conf`という名のHOCONファイルを読み込もうとします。
[コマンドライン引数](#command-line)を利用することでファイルの位置を変更することができます。

<div markdown="1" class="note" style="margin-bottom: 1em;">
以下は`mainClassName`として指定可能で開発エンジンとして利用可能なものです:

* `io.ktor.server.cio.EngineMain`
* `io.ktor.server.tomcat.EngineMain`
* `io.ktor.server.jetty.EngineMain`
* `io.ktor.server.netty.EngineMain`

</div>

Ktorにはどの[module](/servers/application.html#modules)をサーバー起動時にロードしたいのかを`ktor.application.modules`プロパティを使って指定するだけで大丈夫です。
その他のプロパティはオプショナルです。

典型的なシンプルなKtor (`application.conf`)のHOCONファイルは以下のようになります:

```kotlin
ktor {
    deployment {
        port = 8080
    }

    application {
        modules = [ io.ktor.samples.metrics.MetricsApplicationKt.main ]
    }
}
```

ドット記法を使っても同じです:

```kotlin
ktor.deployment.port = 8080
ktor.application.modules = [ io.ktor.samples.metrics.MetricsApplicationKt.main ]
```

Ktorはより様々な設定が行なえます。
Ktorの機能に関わるその他のコアな設定であったり、あなたのアプリケーションのためのカスタムの設定値であったり様々な設定が行えます:

```kotlin
ktor {
    deployment {
        environment = development
        port = 8080
        sslPort = 8443
        autoreload = true
        watch = [ httpbin ]
    }

    application {
        modules = [ io.ktor.samples.httpbin.HttpBinApplicationKt.main ]
    }

    security {
        ssl {
            keyStore = build/temporary.jks
            keyAlias = mykey
            keyStorePassword = changeit
            privateKeyPassword = changeit
        }
    }
}

jwt {
    domain = "https://jwt-provider-domain/"
    audience = "jwt-audience"
    realm = "ktor sample app"
}

youkube {
  session {
    cookie {
      key = 03e156f6058a13813816065
    }
  }
  upload {
    dir = ktor-samples/ktor-samples-youkube/.video
  }
}
```
{: .compact}

このドキュメント内に[設定可能なコア設定の一覧](#available-config)があります。

HOCONを使うことで[プロパティを環境変数から設定](https://github.com/lightbend/config/blob/master/HOCON.md#substitutions)することができます。
{: .note.tip}

[HOCON用のIntelliJプラグイン](https://plugins.jetbrains.com/plugin/10481-hocon)もあるので、インストールすると良いかもしれません。
{: .note.tip}

## コマンドライン
{: #command-line}

[`commandLineEnvironment`](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-host-common/src/io/ktor/server/engine/CommandLine.kt)を使うとき、アプリケーションmoduleを設定する上で利用可能な設定パラメータがいくつかあります。

`-config=anotherfile.conf`を使ってコマンドラインからアプリケーションをを起動したならば、resourceからの代わりにローカルの指定したファイルから設定ファイルを読み込みます。

設定パラメータを使うことでbindされているport番号を上書きすることもできます。

`java -jar myapp-fatjar.jar -port=8080`

[利用可能なコマンドラインの設定パラメータ一覧](#available-config)がこのドキュメント内にあります。

## embeddedServerの設定
{: #embedded-server}

embeddedServerはKtorアプリケーションを起動するための単純な方法です。
あなたが定義したmain関数を提供することで、何が実際に起きるのかを理解するのが簡単になります。

`embeddedServer`はオプショナルなパラメータである`configure`を持っており、第一引数で指定されたエンジンのための設定をセットします。
利用しているエンジンごとに独立しており、いくつかの設定可能なプロパティがあります:

```kotlin
embeddedServer(AnyEngine, configure = {
    // Size of the event group for accepting connections
    connectionGroupSize = parallelism / 2 + 1
    // Size of the event group for processing connections,
    // parsing messages and doing engine's internal work 
    workerGroupSize = parallelism / 2 + 1
    // Size of the event group for running application code 
    callGroupSize = parallelism 
}) {
    // ...
}.start(true)
```

### Multiple connectors

`applicationEngineEnvironment`を使ってコードでいくつかのコネクタを定義することができます。

`applicationEngineEnvironment`の内部で、HTTPとHTTPSのコネクタが定義できます。

*HTTPコネクタの定義:* 

```kotlin
connector {
    host = "0.0.0.0"
    port = 9090
}
```

*HTTPSコネクタの定義:* 

```kotlin
sslConnector(keyStore = keyStore, keyAlias = "mykey", keyStorePassword = { "changeit".toCharArray() }, privateKeyPassword = { "changeit".toCharArray() }) {
    port = 9091
    keyStorePath = keyStoreFile.absoluteFile
}
```

*実際の例:*

```kotlin
fun main(args: Array<String>) {
    val env = applicationEngineEnvironment {
        module {
            main()
        }
        // Private API
        connector {
            host = "127.0.0.1"
            port = 9090
        }
        // Public API
        connector {
            host = "0.0.0.0"
            port = 8080
        }
    }
    embeddedServer(Netty, env).start(true)
}
```

各ApplicationCallのローカルportにアクセスすることで、ローカルportに応じて何をするかを決めることができます:

```kotlin
fun Application.main() {
    routing {
        get("/") {
            if (call.request.local.port == 8080) {
                call.respondText("Connected to public api")
            } else {
                call.respondText("Connected to private api")
            }
        }
    }
}
```

これに関する動作する例は[ktor-samples/multiple-connectors](https://github.com/ktorio/ktor-samples/tree/master/other/multiple-connectors)を見てください。

### Netty
{:.no_toc}

Nettyをエンジンとして使うとき、共通のプロパティに加え、いくつかの他のプロパティも設定できます:

```kotlin
embeddedServer(Netty, configure = {
    // Size of the queue to store [ApplicationCall] instances that cannot be immediately processed
    requestQueueLimit = 16 
    // Do not create separate call event group and reuse worker group for processing calls
    shareWorkGroup = false 
    // User-provided function to configure Netty's [ServerBootstrap]
    configureBootstrap = {
        // ...
    } 
    // Timeout in seconds for sending responses to client
    responseWriteTimeoutSeconds = 10 
}) {
    // ...
}.start(true)
```

### Jetty
{:.no_toc}

Jettyをエンジンとして使うとき、共通のプロパティに加え、いくつかの他のプロパティも設定できます:

```kotlin
embeddedServer(Jetty, configure = {
    // Property to provide a lambda that will be called during Jetty
    // server initialization with the server instance as an argument.
    configureServer = {
        // ...
    } 
}) {
    // ...
}.start(true)
```

### CIO
{:.no_toc}

CIO(Coroutine I/O)をエンジンとして使うとき、共通のプロパティに加え、`connectionIdleTimeoutSeconds`プロパティも設定できます:
When using CIO (Coroutine I/O) as the engine, in addition to common properties, you can configure the `connectionIdleTimeoutSeconds` property.

```kotlin
embeddedServer(CIO, configure = {
    // Number of seconds that the server will keep HTTP IDLE connections open.
    // A connection is IDLE if there are no active requests running.
    connectionIdleTimeoutSeconds = 45
}) {
    // ...
}.start(true)
```

### Tomcat
{:.no_toc}

Tomcatをエンジンとして使うとき、共通のプロパティに加え、いくつかの他のプロパティも設定できます:

```kotlin
embeddedServer(Tomcat, configure = {
    // Property to provide a lambda that will be called during Tomcat
    // server initialization with the server instance as argument.
    configureTomcat { // this: Tomcat ->
        // ...
    }
}) {
    // ...
}.start(true)
```

これらはKtorによって開発されているオフィシャルなエンジンですが、[自作のエンジンを作る](/advanced/engines.html)ことも可能で、それに対しカスタムの設定を作ることも可能です。
{: .note}

## 利用可能な設定パラメータ
{: #available-config}

コマンドラインまたはHOCONファイルから渡すことで、Ktorが追加設定なく利用可能なプロパティ一覧があります。

**Switch**はアプリケーションにわたすコマンドライン引数を参照します。例えばbindされているportを変更するには:

`java -jar myapp-fatjar.jar -port=8080`

**Parameter paths** は`application.conf`内のpathです:

```
ktor.deployment.port = 8080
```

```
ktor {
    deployment {
        port = 8080
    }
}
```

一般的なSwitchとParameterです:

| Switch          | Parameter path                         | Default               | Description |
|-----------------|:---------------------------------------|:----------------------|:------------|
| `-jar=`         |                                        |                       | JARファイルへのパス |
| `-config=`      |                                        |                       | configファイルへのパス（resource内の`application.conf`がデフォルト） |
| `-host=`        | `ktor.deployment.host`                 | `0.0.0.0`             | bindされているhost |
| `-port=`        | `ktor.deployment.port`                 | `80`                  | bindされているport |
| `-watch=`       | `ktor.deployment.watch`                | `[]`                  | リロードを監視するパッケージパス |
|                 | `ktor.application.id`                  | `Application`         | ログ出力する際のアプリケーション識別子 |
|                 | `ktor.deployment.callGroupSize`        | `parallelism`         | Event group size running application code |
|                 | `ktor.deployment.connectionGroupSize`  | `parallelism / 2 + 1` | Event group size accepting connections |
|                 | `ktor.deployment.workerGroupSize`      | `parallelism / 2 + 1` | Event group size for processing connections, parsing messages and doing engine's internal work |
|                 | `ktor.deployment.shutdown.url`         |                       | アプリケーションをシャットダウンするためのURL。内部的に[ShutDownUrl機能](/servers/features/shutdown-url.html)を利用してます。 |
{: .styled-table #general }

SSLポートが定義されているときに必須:

| Switch          | Parameter path                         | Default          | Description |
|-----------------|:---------------------------------------|:-----------------|:------------|
| `-sslPort=`     | `ktor.deployment.sslPort`              | `null`           | SSL port    |
| `-sslKeyStore=` | `ktor.security.ssl.keyStore`           | `null`           | SSL key store |
|                 | `ktor.security.ssl.keyAlias`           | `mykey`          | Alias for the SSL key store |
|                 | `ktor.security.ssl.keyStorePassword`   | `null`           | Password for the SSL key store |
|                 | `ktor.security.ssl.privateKeyPassword` | `null`           | Password for the SSL private key |
{: .styled-table #ssql}

特定のswitchに一致しないパラメータを指定するために`-P:`を使うこともできます。例えば:
`-P:ktor.deployment.callGroupSize=7`.

## コードから設定を読み込む
{: #accessing-config}

`embeddedServer`の代わりに`EngineMain`を使っているなら、HOCONファイルは読み込まれており、設定したプロパティにアクセスすることができます。

アプリケーションを設定するために任意のプロパティのパスを定義することもできます。

```kotlin
val port: String = application.environment.config
    .propertyOrNull("ktor.deployment.port")?.getString()
    ?: "80"
```

commandLineEnvironmentとともにカスタムのmain関数を利用することで、HOCON形式の`application.conf`設定ファイルにもアクセスすることができます:

```kotlin
embeddedServer(Netty, commandLineEnvironment(args + arrayOf("-port=8080"))).start(true)
```

または特定の`EngineMain.main`にリダイレクトすることもできます:

```kotlin
val moduleName = Application::module.javaMethod!!.let { "${it.declaringClass.name}.${it.name}" }
io.ktor.server.netty.main(args + arrayOf("-port=8080", "-PL:ktor.application.modules=$moduleName"))
```

またはカスタムの`applicationEngineEnvironment`と一緒に使えます:

```kotlin
embeddedServer(Netty, applicationEngineEnvironment {
    log = LoggerFactory.getLogger("ktor.application")
    config = HoconApplicationConfig(ConfigFactory.load()) // Provide a Hocon config file

    module {
        routing {
            get("/") {
                call.respondText("HELLO")
            }
        }
    }

    connector {
        port = 8080
        host = "127.0.0.1"
    }
}).start(true)
```

デフォルトの設定ファイルである`application.conf`を明示的に読み込むことで、設定プロパティにアクセスすることもできます:

```kotlin
val config = HoconApplicationConfig(ConfigFactory.load())
```

## 環境変数を使う
{: #environment-variables}

*HOCON*において、環境変数を使っていくつかのパラメータを設定したい場合、`${ENV}`シンタックスを利用して置換を行うことができます。例えば:

```groovy
ktor {
    deployment {
        port = ${PORT}
    }
}
```

このコードは`PORT`環境変数を探します。そして見つからなければ例外を投げます:

```
Exception in thread "main" com.typesafe.config.ConfigException$UnresolvedSubstitution: application.conf @ file:/path/to/application.conf: 3: Could not resolve substitution to a value: ${PORT}
```

環境変数が見つからない場合のプロパティのデフォルト値を指定したい場合は、デフォルト値でプロパティをセットしその後再度`${?ENV}`シンタックスで設定すればよいです:

```groovy
ktor {
    deployment {
        port = 8080
        port = ${?PORT}
    }
}
```

`embeddedServer`を使っている場合は、Javaから`System.getenv`が使えます。例えば:

```kotlin
val port = System.getenv("PORT")?.toInt() ?: 8080
```

## カスタム設定システム
{: #custom}

`application.environment.config`において利用できる、設定を実装するためのインターフェースをKtorは提供しています。
`applicationEngineEnvironment`内で設定プロパティの構築およびセットを行うことができます。

```kotlin
interface ApplicationConfig {
    fun property(path: String): ApplicationConfigValue
    fun propertyOrNull(path: String): ApplicationConfigValue?
    fun config(path: String): ApplicationConfig
    fun configList(path: String): List<ApplicationConfig>
}

interface ApplicationConfigValue {
    fun getString(): String
    fun getList(): List<String>
}

class ApplicationConfigurationException(message: String) : Exception(message)
```

Ktorは2種類の実装を提供しています。１つはMap（`MapApplicationConfig`）をベースにしたもの、もう１つはHOCON（`HoconApplicationConfig`）をベースにしたものです。

configの実装を作成・組み合わせ、`applicationEngineEnvironment`においてそれらをセットすることができます。
そうすることですべてのアプリケーションコンポーネントにおいて利用可能になります。
