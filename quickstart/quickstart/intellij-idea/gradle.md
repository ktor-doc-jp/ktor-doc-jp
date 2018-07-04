---
title: Gradle
caption: Setting up Project in IntelliJ IDEA (Gradle)
category: quickstart
toc: true 
permalink: /quickstart/quickstart/intellij-idea/gradle.html
priority: 0
---

This tutorial will guide you from the most basic setup through to a full
featured setup you can use to start developing your app.

**Table of contents:**

* TOC
{:toc}

## Prerequisites

1.  The most recent version of IntelliJ IDEA
2.  Kotlin and Gradle plugins enabled (They should be enabled by default.)

You can check this in IntelliJ IDEA in the main menu:
* Windows: `File -> Settings -> Plugins`
* Mac: `IntelliJ IDEA -> Settings -> Plugins`

## Start a Project

1.  `File -> New -> Project`:

    ![Ktor IntelliJ: File New Project](/quickstart/intellij-idea/file-new-project.png)

2.  Select Gradle and under Additional Libraries and Frameworks, check Java and Kotlin (Java).  Confirm that Project SDK is completed and click `Next`:

    ![Ktor IntelliJ: Gradle Kotlin JVM](/quickstart/intellij-idea/gradle-kotlin-jvm.png)

3.  Enter a GroupId: `Example`
    and ArtifactId: `Example`
    and click Next:

    ![Ktor IntelliJ: GroupId](/quickstart/intellij-idea/groupid.png)

4.  Check the checkboxes for `Use auto-import` and `Create separate module per source set`. Confirm the Use default gradle wrapper radio button is selected and that Gradle JVM is populated and click `Next`:

    ![Ktor IntelliJ: Gradle Config](/quickstart/intellij-idea/gradle-config.png)

5.  Complete Project name: `Example`
    and Project location: `a/path/on/your/filesystem`
    and click `Finish`:

    ![Ktor IntelliJ: Project Location Name](/quickstart/intellij-idea/project-location-name.png)

6.  Wait a few seconds for Gradle to run, and you should see a project structure like the following (with a few other files and directories):

    ![Ktor IntelliJ: Project Structure](/quickstart/intellij-idea/project-structure.png)

7.  Update your `build.gradle` file with the artifact and repositories for the classes to be available:
    * Include `compile "io.ktor:ktor-server-netty:$ktor_version"`, in your `build.gradle`'s `dependencies` block
    * Include  `maven { url "http://kotlin.bintray.com/ktor" }` and `jcenter()` in your `repositories` block

    ![Ktor IntelliJ: Build Gradle](/quickstart/intellij-idea/build-gradle.png)

For a more detailed guide on setting up the `build.gradle` file, check the [Getting Started with Gradle](/quickstart/quickstart/gradle.html) section. 
{: .note}

## Create the App

Select the `src/main/kotlin` directory and create a new package.  We will call it `blog`.

Select that directory and create a new kotlin file under it named `BlogApp`

![Ktor IntelliJ: Create Kotlin File](/quickstart/intellij-idea/create-kotlin-file.png)

![Ktor IntelliJ: Create Kotlin File Name](/quickstart/intellij-idea/create-kotlin-file-name.png)

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

![Ktor IntelliJ: Program](/quickstart/intellij-idea/program.png)

Now you can Run '`blog.BlogAppKt`'. You can do it, by pressing the glutter icon with the **🐞**{: style="transform:rotate(90deg);display:inline-block;"} symbol and selecting `Debug 'blog.BlogAppKt'`:

![Ktor IntelliJ: Program Run](/quickstart/intellij-idea/program-run.png)

This will also create a run configuration in the upper-right part of IntelliJ, that will allow running
this configuration again easily:

![Ktor IntelliJ: Program Run Config](/quickstart/intellij-idea/program-run-config.png)

This will start the Netty web server.
In your browser enter the URL:  localhost:8080
And you should see your example blog page.

![Ktor IntelliJ: Website](/quickstart/intellij-idea/website.png)

## Improve the app with the Application object

The setup above has a lot of nested blocks and is not ideal for starting to 
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

## Extract out Configuration Data

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

Then we delete the main function from `BlogApp.kt` and change `fun Application.module()` to `fun Application.main()`.  However, if we run the application now, it will fail with an error message like "Top-level function 'main' not found in package blog."  Our `Application.main()` function is now a function extension and does not qualify as a top-level main function.   

This requires us to indicate a new main class as IntelliJ IDEA will no longer be able to find it automatically.  In `build.gradle` we add:

```groovy
apply plugin: 'application'

mainClassName = 'io.ktor.server.netty.DevelopmentEngine'
```

And then go to `Run -> Edit Configurations` select the `blog.BlogAppKt` configuration and change its Main class to:
`io.ktor.server.netty.DevelopmentEngine`

Now when we run the new configuration, the application will start again.
