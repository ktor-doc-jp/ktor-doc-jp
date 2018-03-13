---
title: Docker
caption: Creating Docker Container
section: Quick Start
permalink: /quickstart/docker.html
priority: 0
---

[Docker](https://www.docker.com) is a container platform, a way to package software in a format that can run isolated on a shared operating system.

Publish ktor application to docker is very easy and requires only few things:

* Installed [docker](https://www.docker.com)
* JAR packaging tool

Here we will guide you throw creating docker image and publishing an application to it.

### Package an application

In this tutorial we will use Gradle [shadow plugin](https://github.com/johnrengelman/shadow). It will package
compilation output and all required dependencies into a single JAR file, and append a manifest to tell Java which
function to run first. 

First, you need to add the shadow plugin dependency in your `build.gradle` file:

```groovy 
buildscript {
    ...
    repositories {
        ...
        maven {
          url "https://plugins.gradle.org/m2/"
        }
    }
    dependencies {
        ...
        classpath "com.github.jengelman.gradle.plugins:shadow:2.0.1"
    }

}
```

And after that, you have to apply it, along the application plugin.

```groovy
apply plugin: "com.github.johnrengelman.shadow"
apply plugin: 'application'
``` 

Then, specify main class so it knows what to run when you will tell java inside docker image to run your jar:

```groovy
mainClassName = 'org.sample.ApplicationKt'
```

The string is fully qualified name of the class containing your `main` function. When `main` function is top level
function in a file, class name is file name with the `Kt` suffix. In the example above, `main` function is in the
file `Application.kt` in package `org.sample`.

Finally, configure shadow plugin:

```groovy
shadowJar {
    baseName = 'my-application'
    classifier = null
    version = null
}
```

Now you can run `./gradlew build` to build and package your application. You should get `my-application.jar` 
in `build/libs` folder.  

For more information about configuring this plugin see [documention for the plugin](http://imperceptiblethoughts.com/shadow/)

### Prepare Docker image

In the root folder of your project create file named `Dockerfile` with the following contents:

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
We also select JRE-only image since we don't need to build on image, only run.

```text
COPY ./build/libs/my-application.jar /root/my-application.jar
WORKDIR /root
```

These lines copy your packaged application into Docker image and sets working directory to where we copied it.

```text
CMD ["java", "-server", "-Xms4g", "-Xmx4g", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "my-application.jar"]
```

The last line instructs Docker to run `java` with G1 GC, 4G memory and your packaged application. 

### Building and running an image

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

With this command we start docker in a foreground mode. It will wait for server to exit, or will respond to `Ctrl+C`
to stop. `-it` is telling docker to allocate a terminal (tty) for logs output and responding to interrupt key sequence. 

Since our server is running in a container now, we should tell Docker to expose a port so we can actually access the
server. Parameter `-p 8080:8080` tells docker to publish port 8080 from inside a container as a port 8080 on a local
machine. Thus, when you tell your browser to visit `localhost:8080` it will first reach to Docker and it will bridge
it into internal port `8080` for your application. 

By default a container’s file system persists even after the container exits, so we supply `--rm` option to start clean.

For more information about running a docker image please consult [docker run](https://docs.docker.com/engine/reference/run) 
documentation. 

### Pushing docker image 

After your application has been successfully running locally it may be time to deploy it.

```text
docker tag my-application hub.example.com/docker/registry/tag
docker push hub.example.com/docker/registry/tag
```
 
These commands will tag your application for a registry and push an image. 
Of course, you need to replace `hub.example.com/docker/registry/tag` with an actual URL for your registry.

We won't go into details here since your configuration might require authentication, specific configuration options 
and even special tools. Please consult your organization or cloud platform, or 
check [docker push](https://docs.docker.com/engine/reference/commandline/push/) documentation.