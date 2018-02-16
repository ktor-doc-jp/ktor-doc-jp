---
title: Lifecycle
section: Servers
permalink: /servers/lifecycle.html
caption: What Happens in a Server?  
---

{% include estimated-read-time.html %}

Ktor is designed to be flexible and extensible. It is composited
by simple small pieces, but if you don't know what's happening it is like a black box.

In this section you will discover what is Ktor doing behind the hoods and will know more
about its generic infrastructure. 

**Table of contents:**

* TOC
{:toc}

# Server 

## Entry points

You can run a Ktor application in several ways:

* With a plain `main` by calling `embeddedServer`
* Running a `DevelopmentEngine` `main` function and [using a HOCON `application.conf` configuration file](/servers/configuration.html)
* [As a Servlet within a web server](https://github.com/ktorio/ktor-samples/tree/master/deployment)
* As part of a test using `withTestApplication` from the [`ktor-server-test-host`](https://github.com/ktorio/ktor/tree/master/ktor-server/ktor-server-test-host) artifact

## Start-up

### Common

**[`ApplicationEngineEnvironment`](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-host-common/src/io/ktor/server/engine/ApplicationEngineEnvironment.kt):**

At the beginning this immutable environment has to be built;
with a classLoader, a logger, a [configuration](/servers/configuration.html),
a monitor that acts as an event bus for application events,
and a set of connectors, modules that will form the application and [watchPaths](/servers/autoreload.html).

You can build it using `ApplicationEngineEnvironmentBuilder`,
and handy DSL functions `applicationEngineEnvironment`, `commandLineEnvironment` among others.

**[`ApplicationEngine`](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-host-common/src/io/ktor/server/engine/ApplicationEngine.kt):**

There are multiple `ApplicationEngine`, one per supported server like:
Netty, Jetty, CIO or Tomcat.

The application engine is the class in charge of running the application,
it has a specific **configuration**, an associated **environment** and can be `start`ed and `stop`ped.

When you start a specific application engine, it will use all the configuration
provided to listen, by properly using the specified engine, to the right ports, hosts,
using SSL, certificates and so on, with the specified workers.

Connectors will be used for listening to http/https to specific hosts and ports.
While the `Application` pipeline will be used to handle the requests. 

**[Application](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-core/src/io/ktor/application/Application.kt)** : Pipeline:

It is created by the `ApplicationEngineEnvironment` and it is initially empty.
It is a pipeline without subject that has `ApplicationCall` as context.
Each specified module will be called to configure this application when the
environment is created.

### embeddedServer

When you run your own main method and call the `embeddedServer` function,
you provide a specific `ApplicationEngineFactory` and
a `ApplicationEngineEnvironment` is created or provided.

### DevelopmentEngine

Ktor define one `DevelopmentEngine` class per each supported server engine.
This class defines a `main` method that can be executed to run the application.
By using `commandLineEnvironment` it will load the [HOCON `application.conf`](/servers/configuration.html)
file from your resources and will use extra arguments, to determine which modules to install
and how to configure the server. 

Those classes are normally declared in `CommandLine.kt` source files.

* CIO: [`io.ktor.server.cio.DevelopmentEngine.main`](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-cio/src/io/ktor/server/cio/CommandLine.kt)
* Jetty: [`io.ktor.server.jetty.DevelopmentEngine.main`](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-jetty/src/io/ktor/server/jetty/CommandLine.kt)
* Netty: [`io.ktor.server.netty.DevelopmentEngine.main`](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-netty/src/io/ktor/server/netty/CommandLine.kt)
* Tomcat: [`io.ktor.server.tomcat.DevelopmentEngine.main`](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-tomcat/src/io/ktor/server/tomcat/CommandLine.kt)

### [TestApplicationEngine](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-test-host/src/io/ktor/server/testing/TestApplicationEngine.kt)

For testing, Ktor defines a `TestApplicationEngine` and `withTestApplication` handy method,
that will allow to test your application modules, pipeline and other features without
actually starting any server or mocking any facility.
It will use an in-memory configuration `MapApplicationConfig("ktor.deployment.environment" to "test")`
that you can use to determine if running in a test environment.

## Monitor events

Associated to the environment, there is a monitor instance that Ktor uses to rais application events.
You can use it to subscribe to events. For example you can subscribe to a stop application event
to shutdown specific services or finalize some resources.

A list of [Ktor defined events](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-core/src/io/ktor/application/ApplicationEnvironment.kt):  

```kotlin
val ApplicationStarting = EventDefinition<Application>()
val ApplicationStarted = EventDefinition<Application>()
val ApplicationStopPreparing = EventDefinition<ApplicationEnvironment>()
val ApplicationStopping = EventDefinition<Application>()
val ApplicationStopped = EventDefinition<Application>()
```

## [Pipelines](https://github.com/ktorio/ktor/blob/master/ktor-utils/src/io/ktor/pipeline/Pipeline.kt)

Ktor defines pipelines for asynchronous extensible computations. Pipelines are used all over Ktor.

All the pipelines have associated a **subject** type, a **context** type, and a list of **phases**
with **interceptors** associated to them as well as **attributes** that act as a small typed object container.

Phases are ordered and can be defined to be executed, after or before another phase or at the end.

Each pipeline has an ordered list of phase contexts for that instance that contains a set of
interceptors per phase.

So for example:

* Pipeline
    * Phase1
        * Interceptor1
        * Interceptor2
    * Phase2
        * Interceptor3
        * Interceptor4

The idea here is that each interceptor for a specific phase do not depend on other interceptors
on the same phase, but can depend on interceptors from previous phases.

When executing a pipeline, all the registered interceptors will be executed in the order defined by phases.

### [ApplicationCallPipeline](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-core/src/io/ktor/application/ApplicationCallPipeline.kt)

The server part of Ktor defines an `ApplicationCallPipeline` pipeline without a subject
and with `ApplicationCall` as context.
The `Application` instance is a `ApplicationCallPipeline`.

So when the server's application engine handles a HTTP request, it will execute the `Application`
pipeline.

Th context class `ApplicationCall` contains the application, the `request`, the `response`,
and `attributes` and `parameters`.

In the end, application modules, will end registering interceptors
for specific phases for the Application pipeline, to process the `request` and emitting a `response`.  

The ApplicationCallPipeline defines the following builtin phases for its pipeline:

```kotlin
val Infrastructure = PipelinePhase("Infrastructure") // Phase for setting up infrastructure for processing a call
val Call = PipelinePhase("Call") // Phase for processing a call and sending a response
val Fallback = PipelinePhase("Fallback") // Phase for handling unprocessed calls
```

## [Features](/advanced/features)

Ktor defines application features [`ApplicationFeature`](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-core/src/io/ktor/application/ApplicationFeature.kt).
A feature is something that you can `install` to an specific pipeline.
It has access to the pipeline and it can register interceptors and do all kind of other things. 

## Routing

To illustrate how features and a pipeline tree works together, let's have a look about how routing works.

Routing, like other features, is normally installed like this:

```kotlin
install(Routing) { }
```

But there is a convenience method for registering and start using it that also installs it once if already registered:

```kotlin
routing { }
```

Routing is defined as a tree, where each node is a Route that is also a separate instance of an ApplicationCallPipeline.
So when the root routing node is executed, it wil execute its own pipeline. And will stop executing things once
the route has been processed.
