## Application

An `Application` instance is the main unit of a Ktor Application. When a request comes in (a request can be HTTP, HTTP/2 or Socket requests), it is converted to an `ApplicationCall` and goes through a pipeline which is owned by the `Application`. The pipeline
consists of one or more interceptors that are previously installed, providing certain functionality such as routing, compression, etc.

The `ApplicationCall` provides access to two main properties `ApplicationRequest` and `ApplicationResponse`. As their names indicate, they correspond to the incoming request
and outgoing response. In addition to these, it also provides some useful functions to help response to client requests. Given that pipelines can be executed
asynchronously, `ApplicationCall` also represents the logical execution context with `Attributes` to pass data between various parts of the pipeline.

Installing interceptor into pipeline is the primary method to alter the processing of an `ApplicationCall`.
Nearly all Ktor [Features](Features) are interceptors that perform various operations in different phases of
the application call processing. 

```kotlin
    intercept(ApplicationCallPipeline.Call) { 
        if (call.request.uri == "/")
            call.respondText("Test String")
    }
```
The code above installs an interceptor into the `Call` phase of an `ApplicationCall` processing, and responds with plain text
when the request is asking for a root page.  

This is just an example and usually URI's are not handled in this way, as there is a [routing](Feature-Routing) facility that does this
 and and more. Also, as mentioned previously, defining interceptor is usually done using [Features](Feature) using an `install` function.
   
Most functions available on `ApplicationCall` (such as `respondText` above) are `suspend` functions, indicating that they 
can potentially execute asynchronously.
 
See advanced topic [Pipeline](Advanced-Pipeline) for more information on the mechanics of processing `ApplicationCall`s 

