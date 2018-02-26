---
title: Application
section: Servers
permalink: /servers/application.html
caption: What is an Application? 
---

An `Application` instance is the main unit of a Ktor Application. When a request comes in (a request can be HTTP, HTTP/2 or Socket requests), it is converted to an `ApplicationCall` and goes through a pipeline which is owned by the `Application`. The pipeline
consists of one or more interceptors that are previously installed, providing certain functionality such as routing, compression, etc.

The `ApplicationCall` provides access to two main properties `ApplicationRequest` and `ApplicationResponse`. As their names indicate, they correspond to the incoming request
and outgoing response. In addition to these, it also provides some useful functions to help response to client requests. Given that pipelines can be executed
asynchronously, `ApplicationCall` also represents the logical execution context with `Attributes` to pass data between various parts of the pipeline.

Installing an interceptor into the pipeline is the primary method to alter the processing of an `ApplicationCall`.
Nearly all Ktor [features](/features) are interceptors that perform various operations in different phases of
the application call processing. 

```kotlin
    intercept(ApplicationCallPipeline.Call) { 
        if (call.request.uri == "/")
            call.respondText("Test String")
    }
```
The code above installs an interceptor into the `Call` phase of an `ApplicationCall` processing, and responds with plain text
when the request is asking for a root page.  

This is just an example and usually page requests are not handled in this way, as there is a [routing](/features/routing) facility that does this
 and more. Also, as mentioned previously, defining interceptor is usually done using [features](/features) with an `install` function.
   
Most functions available on `ApplicationCall` (such as `respondText` above) are `suspend` functions, indicating that they 
can potentially execute asynchronously.
 
See advanced topic [Pipeline](/advanced/pipeline) for more information on the mechanics of processing `ApplicationCall`s 

## Modules
{: #modules}

Ktor defines the concept of *module*. A module is just a function receiving the `Application`
class that is in charge of configuring the server, registering routes, handling requests...

You have to specify the modules to load when the server starts in [the `application.conf` file](/servers/configuration.html#hocon-file).

A simple module would look like this:

**Main.kt:**
{: .filename}

```kotlin
package com.example.myapp

fun Application.mymodule() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
}
```

Modules are referenced by its fully qualified name: the fully qualified name of the class, and the method name
separated by a dot.

So for the example, the module's fully qualified name would be:

```
com.example.myapp.MainKt.mymodule
```

Note that `mymodule` is an extension method of the class `Application` (where `Application` is the *receiver*).
Since it is defined as a top-level function, Kotlin creates a JVM class called `FileNameKt`,
and adds the extension method as a static method with the received as its first parameter.
So the class name in this case is `MainKt` and the method `static public void mymodule(Application app)`.
{: .note}
