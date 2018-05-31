---
title: Custom Engines
caption: Create a custom ApplicationEngine
category: advanced
permalink: /advanced/engines.html
keywords: >-
    create custom engine ApplicationEngine
---

Ktor's HTTP client and server provide a common interface while allowing
to use several different engines to perform or to handle HTTP requests.

Ktor includes several artifacts with several engines:
* For the server: `Netty`, `Jetty`, `Tomcat`, `CIO`, `TestEngine`.
* For the client: `ApacheEngine`, `JettyHttp2Engine`, `CIOEngine`, `TestHttpClientEngine`.

## Server's `ApplicationEngine`
{: #server}

### API

A simplified version of the ApplicationEngine looks like this:

```kotlin
interface ApplicationEngineFactory<
    out TEngine : ApplicationEngine,
    TConfiguration : ApplicationEngine.Configuration
> {
    fun create(environment: ApplicationEngineEnvironment, configure: TConfiguration.() -> Unit): TEngine
}

interface ApplicationEngine {
    val environment: ApplicationEngineEnvironment
    fun start(wait: Boolean = false): ApplicationEngine
    fun stop(gracePeriod: Long, timeout: Long, timeUnit: TimeUnit)

    open class Configuration {
        val parallelism: Int
        var connectionGroupSize: Int
        var workerGroupSize: Int
        var callGroupSize: Int
    }
}

interface ApplicationEngineEnvironment {
    val connectors: List<EngineConnectorConfig>
    val application: Application
    fun start()
    fun stop()

    val classLoader: ClassLoader
    val log: Logger
    val config: ApplicationConfig
    val monitor: ApplicationEvents
}

interface EngineConnectorConfig {
    val type: ConnectorType
    val host: String
    val port: Int
}

data class ConnectorType(val name: String) {
    companion object {
        val HTTP = ConnectorType("HTTP")
        val HTTPS = ConnectorType("HTTPS")
    }
}

abstract class BaseApplicationEngine(
    final override val environment: ApplicationEngineEnvironment,
    val pipeline: EnginePipeline = defaultEnginePipeline(environment)
) : ApplicationEngine {
    val application: Application
}
```
{:.compact}

### The `ApplicationEngineFactory`

Each implementation of the `ApplicationEngineFactory` along the a subtyped `ApplicationEngine.Configuration`
are the public exposed APIs for each engine.

```kotlin
fun ApplicationEngineFactory.create(environment: ApplicationEngineEnvironment, configure: TConfiguration.() -> Unit): TEngine
```

The `ApplicationEngineFactory.create` is in charge of instantiating the right subtyped `ApplicationEngine.Configuration`,
and call the provided `configure: TConfiguration.() -> Unit` lambda that should mutate the configuration object,
and constructs an implementation of the `ApplicationEngine` probably subtype of `BaseApplicationEngine`.

For example:

```kotlin
class MyApplicationEngineFactory
    <MyApplicationEngine, MyApplicationEngineConfiguration>
{

    fun create(
        environment: ApplicationEngineEnvironment,
        configure: MyApplicationEngineConfiguration.() -> Unit
    ): MyApplicationEngine {
    
        val configuration = MyApplicationEngineConfiguration()
        configure(configuration)
        return MyApplicationEngine(environment, configuration)
    }
    
}
```

### The `ApplicationEngine` and `BaseApplicationEngine`

The interface `ApplicationEngine`, with an abstract implementation `BaseApplicationEngine` is the one in charge
of starting and stopping the application. It holds the `ApplicationEngineEnvironment` and the constructed
configuration of the application.

This class has two methods:

* The `start` method: it should connect to the `ApplicationEngineEnvironment.connectors` that come from the environment,
  start the environment, and to start and configure the engine to trigger an execute of the `application` pipeline
  for each HTTP request with an `ApplicationCall`.
* The `stop` method: should stop the engine, the environment and to unregister all the things registered by the start method.

The `BaseApplicationEngine` exposes an `ApplicationEngineEnvironment` passed to the constructor, while
creates an `EnginePipeline` used as intermediary to pre-intercept the application pipeline. It also installs 
default transformations the the receive and end pipelines and logs the defined connection endpoints.

For example:

```kotlin
class MyApplicationEngine(
    environment: ApplicationEngineEnvironment,
    configuration: MyApplicationEngineConfiguration
) : BaseApplicationEngine(environment) {
    val myEngine = MyEngine()

    override fun start(wait: Boolean): MyApplicationEngine {
        environment.start()
        myEngine.start()
        myEngine.addRequestHandler(::handleRequest)
        return this
    }
    
    override fun stop(gracePeriod: Long, timeout: Long, timeUnit: TimeUnit) {
        myEngine.removeRequestHandler(::handleRequest)
        myEngine.stop()
        environment.stop()
    }
    
    private fun handleRequest(request: MyEngineCall) {
        val call: ApplicationCall = request.toApplicationCall()
        pipeline.execute(call)
    }
}

```
