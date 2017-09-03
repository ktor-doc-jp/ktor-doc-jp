---
title: Using IntelliJ IDEA 
tags: [overview]
sidebar: mydoc_sidebar
permalink: /getting-started-idea.html
summary: Explains how to get up and running with IntelliJ IDEA
---

This tutorial will guide you from the most basic setup to a full featured setup you can use to start developing your app.

## Prerequisites

1.  The most recent version of Intellij IDEA
2.  Kotlin and Gradle plugins enabled (They should be by default.)

Check this in IDEA at:  File -> Settings -> Plugins

## Start a Project

1.  File -> New -> Project
2.  Select Gradle and under Additional Libraries and Frameworks, check Java and Kotlin (Java).  Confirm that Project SDK is completed and click Next.
3.  Enter a GroupId: Example  
and ArtifactId: Example  
and click Next
4.  Check the checkboxes for "Use auto-import", "Create directories for empty content roots automatically", and "Create separate module per source set".  Confirm the Use default gradle wrapper radio button is selected and that Gradle JVM is populated and click Next.
5.  Complete Project name: Example  
and Project location: a/path/on/your/filesystem   
and click Finish
6.  Wait a few seconds for Gradle to run and you should see a project structure like the following (with a few other files and directories):

```
Example 
  src
    main
        java
        kotlin
        resources
    test
        java
        kotlin
        resources
  build.gradle
```

For more detailed guide on setting up build files see

* [Getting Started with Gradle](getting-started-gradle)
* [Getting Started with Maven](getting-started-maven)

## Create the App

Select the src/main/kotlin directory and create a new package.  We will call it blog.

Select that directory and create a new kotlin file under it named BlogApp

Copy and paste in the most basic setup for an app so that it looks like:

```kotlin
package blog

import org.jetbrains.ktor.netty.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.host.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.response.*

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080) {
        routing {
            get("/") {
                call.respondText("My Example Blog", ContentType.Text.Html)
            }
        }
    }.start(wait = true)
}
```

Now you can Run 'blog.BlogAppKt'  

This will start the Netty web server.
In your browser enter the url:  localhost:8080
And you should see your example blog plage.

### Improve the app with the Application object

The setup above has a lot of nested blocks and is not the best for starting to 
add functionality to your app.  We can improve it by using the Application object 
and referring to that from an embeddedServer call in the main function.  

Change your code in BlogApp.kt to the following to try this:

```kotlin
package blog

import org.jetbrains.ktor.netty.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.features.DefaultHeaders
import org.jetbrains.ktor.host.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.logging.CallLogging
import org.jetbrains.ktor.response.*

fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)
    install(Routing) {
        get("/") {
            call.respondText("My Example Blog2", ContentType.Text.Html)
        }
    }
}

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080, watchPaths = listOf("BlogAppKt"), module = Application::module).start()
}
```

### Extract out Configuration Data

Although we can designate some application configuration data in the main function embeddedServer call, we can provide more flexibility for future deployments and changes by extracting this out to a separate configuration file.  In the src/main/resources directory we will create a new text file named application.conf with the following content:

```json
ktor {
    deployment {
        port = 8080
    }

    application {
        modules = [ blog.BlogAppKt.main ]
    }
}
```

Then we delete the main function from BlogApp.kt and change fun Application.module() to fun Application.main().  However, if we run the application now it will fail with an error message like "Top-level function 'main' not found in package blog."  Our Application.main() function is now a function extension and does not qualify as a top-level main function.   

This requires us to indicate a new main class as IDEA will no longer be able to find it automatically.  In build.gradle we add:

```groovy
apply plugin: 'application'

mainClassName = 'org.jetbrains.ktor.netty.DevelopmentHost'
```

And then go to Run -> Edit Configurations -> select the blog.BlogAppKt configuration and change its Main class to:
org.jetbrains.ktor.netty.DevelopmentHost

Now when we run the new configuration, the application will start again.
