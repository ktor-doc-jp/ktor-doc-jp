---
title: Configuration
caption: Configuring Server   
section: Servers
permalink: /servers/configuration.html
---

Ktor uses kotlin lambdas for configuring the application and when using embeddedServer.

And uses [HOCON (Human-Optimized Config Object Notation)](https://github.com/lightbend/config/blob/master/HOCON.md) format for its configuration file.
This format is similar to JSON, but optimized for being read and written by humans. And supports things like environment variable substitution.

**Table of contents:**

* [Configuring embeddedServer](#embedded-server)
* [Accessing the configuration](#accessing-config)
* [Command Line](#command-line)
* [Custom configuration systems](#custom)

<a id="embedded-server"></a>
## Configuring embeddedServer

`embeddedServer` includes an optional parameter `configure` specific per factory where you can set generic
and specific configuration. Any configuration will have these common available fields to set:

```kotlin
open class Configuration {
    val parallelism = Runtime.getRuntime().availableProcessors() // Provides currently available parallelism, e.g. number of available processors
    var connectionGroupSize = parallelism / 2 + 1 // Specifies size of the event group for accepting connections
    var workerGroupSize = parallelism / 2 + 1 // Specifies size of the event group for processing connections, parsing messages and doing engine's internal work
    var callGroupSize = parallelism // Specifies size of the event group for running application code
}
```

### Netty

```kotlin
class Configuration : BaseApplicationEngine.Configuration() {
    var requestQueueLimit: Int = 16 // Size of the queue to store [ApplicationCall] instances that cannot be immediately processed
    var shareWorkGroup: Boolean = false // Do not create separate call event group and reuse worker group for processing calls
    var configureBootstrap: ServerBootstrap.() -> Unit = {} // User-provided function to configure Netty's [ServerBootstrap]
    var responseWriteTimeoutSeconds: Int = 10 // Timeout in seconds for sending responses to client
}
```

### Jetty

```kotlin
class Configuration : BaseApplicationEngine.Configuration() {
    var configureServer: Server.() -> Unit = {}
}
```

### CIO

```kotlin
class Configuration : BaseApplicationEngine.Configuration() {
    var connectionIdleTimeoutSeconds: Int = 45
}
    
```

### Tomcat

```kotlin
class Configuration : BaseApplicationEngine.Configuration() {
    var configureTomcat: Tomcat.() -> Unit = {}
}
```

<a id="accessing-config"></a>
## Accessing the configuration

When using a `DevelopmentEngine` (instead of an `embeddedServer`), the HOCON file is loaded,
and you access to its configurations.

You can also define arbitrary property paths to configure your application.

```kotlin
val port: String = application.environment.config.propertyOrNull("ktor.deployment.port")?.getString() ?: "80"
```

It is possible to access the HOCON `application.conf` configuration too using with a custom main with commandLineEnvironment:

```kotlin
embeddedServer(Netty, commandLineEnvironment(args + arrayOf("-port=8080"))).start(true)
```

Or by redirecting to the specific `DevelopmentEngine.main`:

```kotlin
val moduleName = Application::module.javaMethod!!.let { "${it.declaringClass.name}.${it.name}" }
io.ktor.server.netty.main(args + arrayOf("-port=8080", "-PL:ktor.application.modules=$moduleName"))
```

Or with a custom `applicationEngineEnvironment`:

```kotlin
embeddedServer(Netty, applicationEngineEnvironment {
    this.log = LoggerFactory.getLogger("ktor.application")
    this.config = HoconApplicationConfig(ConfigFactory.load()) // Provide a Hocon config file

    this.module {
        routing {
            get("/") {
                call.respondText("HELLO")
            }
        }
    }

    connector {
        this.port = 8080
        this.host = "127.0.0.1"
    }
}).start(true)
```

You can also access the configuration by manually loading the default config file `application.conf`:

```kotlin
val config = HoconApplicationConfig(ConfigFactory.load())
``` 

<a id="command-line"></a>
## Command Line

When using [`commandLineEnvironment`](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-host-common/src/io/ktor/server/engine/CommandLine.kt)
(any `DevelopmentEngine` main) there are several switches and configuration parameters you can use to configure
your application module.

**Switch** refers to arguments that you pass to the application, so yo can for example change the host by:

`java -jar myapp-fatjar.jar -port=8080`

**Parameter paths** are paths inside the HOCON `application.conf` file:

```
ktor {
    deployment {
        port = 8080
    }
}
```

### Available configuration parameters:

| Switch          | Description |
|-----------------|:------------|
| `-jar=`         | Path to JAR file |
| `-config=`      | Path to config file (instead of `application.conf` from resources) |

| Switch          | Parameter path                         | Default             | Description |
|-----------------|:---------------------------------------|:--------------------|:------------|
| `-host=`        | `ktor.deployment.host`                 | `0.0.0.0`           | Binded host |
| `-port=`        | `ktor.deployment.port`                 | `80`                | Binded port |
| `-watch=`       | `ktor.deployment.watch`                | `[]`                | Package paths to watch for reloading |
|                 | `ktor.application.id`                  | `Application`       | Application Identifier used for logging |
|                 | `ktor.deployment.callGroupSize`        | parallelism         | Event group size running application code |
|                 | `ktor.deployment.connectionGroupSize`  | parallelism / 2 + 1 | Event group size accepting connections |
|                 | `ktor.deployment.workerGroupSize`      | parallelism / 2 + 1 | Event group size for processing connections, parsing messages and doing engine's internal work |

Required when SSL port is defined:

| Switch          | Parameter path                         | Default          | Description |
|-----------------|:---------------------------------------|:-----------------|:------------|
| `-sslPort=`     | `ktor.deployment.sslPort`              | `null`           | SSL port    |
| `-sslKeyStore=` | `ktor.security.ssl.keyStore`           | `null`           | SSL key store |
|                 | `ktor.security.ssl.keyAlias`           | `mykey`          | Alias for the SSL key store |
|                 | `ktor.security.ssl.keyStorePassword`   | `null`           | Password for the SSL key store |
|                 | `ktor.security.ssl.privateKeyPassword` | `null`           | Password for the SSL private key |

You can use `-P:` to specify parameters that doesn't have a specific switch. For example:
`-P:ktor.deployment.callGroupSize=7`

<a id="custom"></a>
## Custom configuration systems

Ktor provides an interface that you can implement for its configuration available at `application.environment.config`.
You can construct and set the configuration inside an `applicationEngineEnvironment`.

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

Ktor provides two implementations. One based in a map (`MapApplicationConfig`), and other based in HOCON (`HoconApplicationConfig`).

You can create and compose config implementations and set them at `applicationEngineEnvironment` so it is available to all the
application components.
