---
title: Application
caption: Creating Your First Application
section: Quick Start
permalink: /quickstart/application.html
priority: 0
---

This tutorial will guide you through the steps on how to create a simple self-hosted Ktor server application that responds to HTTP requests with `Hello, World!`.
Ktor applications can be built using common build systems such as [Maven](https://kotlinlang.org/docs/reference/using-maven.html) or [Gradle](https://kotlinlang.org/docs/reference/using-gradle.html).

**Table of contents:**

* TOC
{:toc}

## Including the right dependencies
{: #dependencies }

Ktor is split up into several groups of modules,
allowing you to include only the functionality that you will need.
 
For a list of these modules, please check the [Artifacts](/artifacts) page.
In this case you only need to include `ktor-server-netty`.  

These dependencies are hosted on [Bintray](https://bintray.com/kotlin/ktor) and as such the right
repositories need to be added to your build script.

For more detailed guide on setting up build files with different build systems see:

* [Setting up Gradle Build](/quickstart/gradle.html)
* [Setting up Maven Build](/quickstart/maven.html)

## Creating a self-hosted Application
{: #self-hosted}

Ktor allows applications to run within an Application Server compatible with Servlets such as Tomcat,
or as an embedded application, using Jetty or Netty.

In this tutorial you will learn how to self-host an application using Netty.

You can start by creating an `embeddedServer`, passing in the engine factory as the first argument,
the port as the second argument and the actual application code as the fourth argument (third argument
is the host which is 0.0.0.0 by default).

The code below defines a single route that responds to the `GET` verb on the url `/` with
the text `Hello, world!`

After defining the routes, you have to start the server by calling `server.start`,
passing as argument a boolean to indicate whether you want the main thread
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

If you server is just listening for HTTP requests and do not want do do anything else after that in the setup,
you will normaly call the server.start with `wait = true`.
{: .note}

## Running the Application
{: #running }

Given that the entry point of your application is the standard Kotlin `main` function, 
you can simply run it, effectively starting the server and listening on the specified port.

Checking the `localhost:8080` page in your browser, you should see the `Hello, world!` text. 

## Next Steps
{: #next-steps }

This was the simplest example of getting a self-hosted Ktor application up and running. 
A recommended tour to continue learning Ktor on the server would be:

* [What is an Application?](/servers/application.html)
* [Features](/features)
* [Application Structure](/servers/structure.html)
* [Testing](/servers/testing.html)
