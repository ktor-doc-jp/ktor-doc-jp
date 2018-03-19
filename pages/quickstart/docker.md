---
title: Docker
caption: Creating Docker Container
section: Quick Start
permalink: /quickstart/docker.html
priority: 0
---

[Docker](https://www.docker.com) is a container platform:
it allows to package package software in a format,
and to run it isolated in any supported operating system.

Publish a Ktor application to docker is very easy and requires only a few steps:

* Install [Docker](https://www.docker.com)
* A JAR packaging tool

In this page we will guide you through creating a docker images and publishing an application to it.

**Table of contents:**

* TOC
{:toc}

### Package an application using Gradle

In this tutorial we will use the Gradle [shadow plugin](https://github.com/johnrengelman/shadow).
It packages all the output classes, resources and all the required dependencies into a single JAR file,
and appends a manifest file to tell Java which is the entry-point main class containing the main method. 

First, you need to add the shadow plugin dependency in your `build.gradle` file:

```groovy 
buildscript {
    ...
    repositories {
        ...
        maven { url "https://plugins.gradle.org/m2/" }
    }
    dependencies {
        ...
        classpath "com.github.jengelman.gradle.plugins:shadow:2.0.1"
    }
}
```

After that, you have to apply it, along the application plugin:

```groovy
apply plugin: "com.github.johnrengelman.shadow"
apply plugin: 'application'
``` 

Then specify main class, so it knows what to run when running the java's JAR inside Docker:

```groovy
mainClassName = 'org.sample.ApplicationKt'
```

The string is the fully qualified name of the class containing your `main` function. When `main` function is a top level
function in a file, the class name is the file name with the `Kt` suffix. In the example above, `main` function is in the
file `Application.kt` in package `org.sample`.

Finally, you have to configure the shadow plugin:

```groovy
shadowJar {
    baseName = 'my-application'
    classifier = null
    version = null
}
```

Now you can run `./gradlew build` to build and package your application.
You should get `my-application.jar` in `build/libs` folder.  

For more information about configuring this plugin see [documention for the plugin](http://imperceptiblethoughts.com/shadow/)

So a full `build.gradle` file would look like this:


**`build.gradle`**:
```groovy
buildscript {
    ext.kotlin_version = '{{site.kotlin_version}}'
    ext.ktor_version = '{{site.ktor_version}}'
    ext.logback_version = '1.2.1'
    ext.slf4j_version = '1.7.25'
    repositories {
        jcenter()
        maven { url "https://plugins.gradle.org/m2/" }
    }
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        classpath "com.github.jengelman.gradle.plugins:shadow:2.0.1"
    }
}

apply plugin: 'kotlin'
apply plugin: "com.github.johnrengelman.shadow"
apply plugin: 'application'

mainClassName = "io.ktor.server.netty.DevelopmentEngine"

sourceSets {
    main.kotlin.srcDirs = [ 'src' ]
    main.resources.srcDirs = [ 'resources' ]
}

repositories {
    jcenter()
    maven { url "http://kotlin.bintray.com/ktor" }
}

dependencies {
    compile "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
    compile "io.ktor:ktor-server-netty:$ktor_version"
    compile "io.ktor:ktor-html-builder:$ktor_version"
    compile "ch.qos.logback:logback-classic:$logback_version"
}

kotlin.experimental.coroutines = 'enable'

shadowJar {
    baseName = 'my-application'
    classifier = null
    version = null
}
```
{: .compact}

**`resources/application.conf`**:
```groovy
ktor {
    deployment {
        port = 8080
    }

    application {
        modules = [ io.ktor.samples.hello.HelloApplicationKt.main ]
    }
}
```
{: .compact}

**`src/HelloApplication.kt`**:
```kotlin
package io.ktor.samples.hello

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.routing.*
import kotlinx.html.*

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    routing {
        get("/") {
            call.respondHtml {
                head {
                    title { +"Ktor: jetty" }
                }
                body {
                    p {
                        +"Hello from Ktor Jetty engine sample application"
                    }
                }
            }
        }
    }
}
```
{: .compact}

You can check this [full example](https://github.com/ktorio/ktor-samples/tree/master/deployment/docker) at the ktor-samples repository.
{: .note }

### Prepare Docker image

In the root folder of your project create a file named `Dockerfile` with the following contents:

```text
FROM openjdk:8-jre-alpine

COPY ./build/libs/my-application.jar /root/my-application.jar

WORKDIR /root

CMD ["java", "-server", "-Xms4g", "-Xmx4g", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "my-application.jar"]
```

Let's see what is what:

```text
FROM openjdk:8-jre-alpine
```

This line tells Docker to base an image on a pre-built image with [Alpine Linux](https://alpinelinux.org/). You can use other images 
from [OpenJDK registry](https://hub.docker.com/_/openjdk/). Alpine Linux benefit is that the image is pretty small. 
We also select JRE-only image since we don't need to compile code on the image, only run precompiled classes.

```text
COPY ./build/libs/my-application.jar /root/my-application.jar
WORKDIR /root
```

These lines copy your packaged application into the Docker image and sets working directory to where we copied it.

```text
CMD ["java", "-server", "-Xms4g", "-Xmx4g", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "my-application.jar"]
```

The last line instructs Docker to run `java` with G10s GC, 4G of memory and your packaged application. 

### Building and running the Docker image

Build an application package:

```
./gradlew build
```

Build and tag an image:

```
docker build -t my-application .
```

Start an image:

```
docker run -it -p 8080:8080 --rm my-application
```

With this command we start docker in a foreground mode. It will wait for server to exit, also it
will respond to `Ctrl+C` to stop it. `-it` instructs docker to allocate a terminal (*tty*) to pipe the stdout
and to respond to the interrupt key sequence. 

Since our server is running in an isolated container now, we should tell Docker to expose a port so we can
actually access the server port. Parameter `-p 8080:8080` tells docker to publish port 8080 from inside a container as a port 8080 on a local
machine. Thus, when you tell your browser to visit `localhost:8080` it will first reach to Docker and it will bridge
it into internal port `8080` for your application. 

By default a container’s file system persists even after the container exits, so we supply `--rm` option to prevent
piling garbage.

For more information about running a docker image please consult [docker run](https://docs.docker.com/engine/reference/run) 
documentation.

### Pushing docker image 

Once your application is running locally successfully, it might be a time to deploy it:

```text
docker tag my-application hub.example.com/docker/registry/tag
docker push hub.example.com/docker/registry/tag
```
 
These commands will tag your application for a registry and push an image. 
Of course, you need to replace `hub.example.com/docker/registry/tag` with an actual URL for your registry.

We won't go into details here since your configuration might require authentication, specific configuration options 
and even special tools. Please consult your organization or cloud platform, or 
check [docker push](https://docs.docker.com/engine/reference/commandline/push/) documentation.

### Sample

You can check a [full sample](https://github.com/ktorio/ktor-samples/tree/master/deployment/docker) at the ktor-samples repository.
