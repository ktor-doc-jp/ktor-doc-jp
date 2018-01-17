---
title: Application
caption: Creating First Application
section: Quick Start
permalink: /quickstart/application.html
---

This tutorial will guide you through the steps on how to create a simple self-hosted Ktor server application that responds to HTTP requests with `Hello, World!`.
Ktor applications can be built using common build systems such as [Maven](https://kotlinlang.org/docs/reference/using-maven.html) or [Gradle](https://kotlinlang.org/docs/reference/using-gradle.html).

### Including the right dependencies

Ktor is split up into several groups of modules, allowing us to include only the functionality that we need. 
For a list of these modules, please see [Artifacts](/artifacts).
In our case we only need to include `ktor-server-netty`.  

These dependencies are hosted on [Bintray](https://bintray.com/kotlin/ktor) and as such the right
repositories need to be added to our build script.

For more detailed guide on setting up build files with different build systems see

* [Setting up Gradle Build](gradle)
* [Setting up Maven Build](maven)

### Creating a self-hosted Application

Ktor allows applications to be running with Application Server such as Tomcat, or as an embedded application, using Jetty or Netty.
In this tutorial we're going to see how to self-host using Netty.

We begin by creating an `embeddedServer`, passing in the engine factory as the first argument, the port as the second argument and the actual application code as the fourth argument (third argument is the host which is 0.0.0.0 by default).
The code below defines a single route that responds to the `GET` verb on the url `/` with the text `Hello, world!`

Once we've defined the routes, we start the server by calling `server.start`, passing as argument a boolean to indicate whether we want the main thread
of the application to block.  

```kotlin
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    val server = embeddedServer(Netty, 8080) {
        routing {
            get("/") {
                call.respondText("Hello, world!", ContentType.Text.Html)
            }
        }
    }
    server.start(wait = true)
}
```

### Running the Application

Given that the entry point to our application is the standard Kotlin `main` function, 
we can simply run it and have our server start, listening on the designated port.

When you point your browser to `localhost:8080` you should see `Hello, world!` text. 

### Next Steps

This was the simplest example of getting a self-hosted Ktor application up and running. 
A recommended tour to continue learning Ktor on the server would be:

* [What is an Application?](/servers/application)
* [Features](/features)
* [Engines Options](/servers/hosting)
* [Application Structure](/servers/structure)
* [Testing](/servers/testing)
