---
title: IntelliJ IDEA 
caption: Setting up Project in IntelliJ IDEA
section: Quick Start
permalink: /quickstart/intellij-idea.html
---

This tutorial will guide you from the most basic setup to a full featured setup you can use to start developing your app.

### Prerequisites

1.  The most recent version of Intellij IDEA
2.  Kotlin and Gradle plugins enabled (They should be by default.)

Check this in IDEA at:  _**File -> Settings -> Plugins**_

### Start a Project

1.  **_File -> New -> Project_**
2.  Select Gradle and under Additional Libraries and Frameworks, check Java and Kotlin (Java).  Confirm that Project SDK is completed and click Next.
3.  Enter a GroupId: **Example**  
    and ArtifactId: **Example**  
    and click Next
4.  Check the checkboxes for "_Use auto-import_", "_Create directories for empty content roots automatically_", and "_Create separate module per source set_".  Confirm the Use default gradle wrapper radio button is selected and that Gradle JVM is populated and click Next.
5.  Complete Project name: **Example**  
    and Project location: _a/path/on/your/filesystem_   
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

You will have to include `compile "io.ktor:ktor-server-netty:$ktor_version"`
in your `build.gradle`'s `dependencies` block, for the classes in the example to be available.

For a more detailed guide on setting up the `build.gradle` file, check the [Getting Started with Gradle](/quickstart/gradle) section. 
{: .note}

### Create the App

Select the `src/main/kotlin` directory and create a new package.  We will call it `blog`.

Select that directory and create a new kotlin file under it named `BlogApp`

Copy and paste in the most basic setup for an app so that it looks like:

```kotlin
package blog

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

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

Now you can Run '`blog.BlogAppKt`'  

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

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

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

Although we can designate some application configuration data in the main function embeddedServer call, we can provide more flexibility for future deployments and changes by extracting this out to a separate configuration file.  In the `src/main/resources` directory we will create a new text file named `application.conf` with the following content:

```kotlin
ktor {
    deployment {
        port = 8080
    }

    application {
        modules = [ blog.BlogAppKt.main ]
    }
}
```

Then we delete the main function from `BlogApp.kt` and change fun `Application.module()` to `fun Application.main()`.  However, if we run the application now it will fail with an error message like "Top-level function 'main' not found in package blog."  Our `Application.main()` function is now a function extension and does not qualify as a top-level main function.   

This requires us to indicate a new main class as IDEA will no longer be able to find it automatically.  In `build.gradle` we add:

```groovy
apply plugin: 'application'

mainClassName = 'io.ktor.server.netty.DevelopmentEngine'
```

And then go to _**Run -> Edit Configurations**_ select the `blog.BlogAppKt` configuration and change its Main class to:
`io.ktor.server.netty.DevelopmentEngine`

Now when we run the new configuration, the application will start again.
