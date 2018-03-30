---
title: Quick Start
caption: QuickStart
section: Quick Start
permalink: /quickstart/index.html
ktor_version: 0.9.1
priority: -1
---

![Ktor logo](/assets/images/ktor_logo.svg){:style="width:134px;height:56px;"}
 
Ktor is a framework to easily build connected applications – web applications, HTTP services, mobile and browser applications.
Modern connected applications need to be asynchronous to provide best experience to users, and Kotlin coroutines provide
awesome facilities to do it in an easy and straightforward way. 

While not yet entirely there, Ktor goal is to provide end-to-end multiplatform application framework for connected applications. 
Currently JVM client and server scenarios are supported, and we are working to bringing server facilities to native
environments, and client facilities to native and JavaScript.

{::comment}
Ktor embraces strongly typed nature of Kotlin programming language and provides [strongly typed end-points (Locations)](/features/locations.html) and
ability to exchange data with classes shared across platforms.
{:/comment}

**Table of contents:**

* TOC
{:toc}

{::comment}
## start.ktor.io

Ktor has a [start.ktor.io](https://soywiz.github.io/start-ktor-io-proposal/) website to quickly generate a ZIP with a skeleton for your application:

<iframe src="https://soywiz.github.io/start-ktor-io-proposal/" style="border:1px solid #aaa;width:100%;height:450px;"></iframe>
{:/comment}

## Gradle Setup

This section asumes basic knowledge about gradle. If you have never used gradle,
gradle.org provides [several guides](https://guides.gradle.org/building-java-applications/) for you to start.
{: .note}

You can set-up a simple Ktor application using gradle like this:

![Ktor Build with Gradle](/pages/quickstart/1/ktor_build_gradle.png)

Text version:
```groovy
group 'Example'
version '1.0-SNAPSHOT'

buildscript {
    ext.kotlin_version = '1.2.30'
    ext.ktor_version = '0.9.1'

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}

apply plugin: 'java'
apply plugin: 'kotlin'
apply plugin: 'application'

mainClassName = 'MainKt'

sourceCompatibility = 1.8
compileKotlin { kotlinOptions.jvmTarget = "1.8" }
compileTestKotlin { kotlinOptions.jvmTarget = "1.8" }

kotlin { experimental { coroutines "enable" } }

repositories {
    mavenCentral()
    jcenter()
    maven { url "https://dl.bintray.com/kotlin/ktor" }
}

dependencies {
    compile "org.jetbrains.kotlin:kotlin-stdlib-jre8:$kotlin_version"
    compile "io.ktor:ktor-server-netty:$ktor_version"
    compile "ch.qos.logback:logback-classic:1.2.1"
    testCompile group: 'junit', name: 'junit', version: '4.12'
}
```
{: .compact}

Since Ktor is not 1.0 yet, we have custom maven repositories for distributing our early preview artifacts.
You have to set up a couple of repositories as shown below, so your tools can find ktor artifacts and dependencies.

Of course don't forget to include the actual artifact! For our quickstart we are using the `ktor-server-netty` artifact.
That includes the Ktor's core, netty and the ktor-netty connector as transitive dependencies.
You can of course include all the additional dependencies that you need.

Since ktor is designed to be modular, you will require additional artifacts and potentially other repositories
for specific features. You will find the required artifacts (and repositories when required) for each feature in the
specific feature documentation.
{:.note}

## Hello World

A simple hello world in Ktor would look like this:

![Ktor Hello World](/pages/quickstart/1/ktor_hello_world_main.png)

1. Here you define a plain callable *main method*.
2. Then you create an embedded *server using Netty* as backend that will listen on *port 8080*.
3. Installs the *routing feature* with a block where you can define routes for specific paths and http methods.
4. Actual routes: In this case it will handle a *GET request* for the path `/demo`, and will reply with a `HELLO WORLD!` message.
5. Actually *starts the server* and wait for connections.

Text version:
```kotlin
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    val server = embeddedServer(Netty, port = 8080) {
        routing {
            get("/") {
                call.respondText("Hello World!", ContentType.Text.Plain)
            }
            get("/demo") {
                call.respondText("HELLO WORLD!")
            }
        }
    }
    server.start(wait = true)
}
```
{: .compact}

## Accessing your application

Since you have a main method, you can just execute it with your IDE. That will open a http server,
listening at [http://127.0.0.1:8080](http://127.0.0.1:8080/) You can try opening it with your favorite web browser.

If that doesn't work, maybe you are using that port already. You can try changing the
port 8080 (in line 10) to adjust it as needed.
{: .note}

![Ktor Hello World Browser](/pages/quickstart/1/screenshot.png){: width="50%""}

At this point you should have a very simple Web Backend running, so you can make changes,
and see the results in the browser.

Since you have configured a gradle project with the application plugin and the `mainClassName`,
you can also run it from a terminal using `./gradlew run` on Linux/Mac, or `gradlew run` on a windows machine.
{:.note}

{::comment}
## Next step

Now we are ready for the next step. *What kind of application are you developing?*

1. [RESTful API: Let's serve a *data class* as JSON](/quickstart/restful.html)
2. Web Application:
    * [Let's describe and serve some html, fully typed, using kotlinx.html the DSL way](/quickstart/html-dsl.html)
    * [Let's serve some html using FreeMarker template engine](/quickstart/html-freemarker.html)
    
{:/comment}

## Walkthroughs

{% include category-list.html %}
