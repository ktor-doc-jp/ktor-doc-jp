---
title: Running
caption: Running Server Application 
section: Servers
permalink: /servers/engine.html
---

Ktor applications can be self-hosted or hosted in an Application Server. This section shows to how host Ktor applications externally.

**Table of contents:**

* TOC
{:toc}

### Running an application in an external host

When you need to run a Ktor application in an independently maintained host (for instance Tomcat), you will need an `application.conf` file
to tell Ktor how to start your application. 

#### Defining the configuration

In the resources folder create a file named `application.conf` with the following contents

```kotlin
ktor {
    deployment {
        port = 8080
    }

    application {
        modules = [ my.company.MyApplication.ApplicationKt.main ]
    }
}
```

Replace `my.company.MyApplication` with your application's package, and `ApplicationKt` with then name of the
file your `Application.main` function is contained in.

#### Deploying the hosted application

// TODO 

### Running the application from inside the IDE

Running applications in a development environment such as IntelliJ IDEA, is supported by using development engines. 

##### IntelliJ IDEA 

1. Create a new Run Configuration using "Application" as template
2. For the main class use one of the following engines
  * Netty: use `io.ktor.server.netty.DevelopmentEngine` 
  * Jetty: use `io.ktor.server.jetty.DevelopmentEngine` 
3. Specify the Module to be used
4. Save the Configuration by giving it a name

Once the configuration is saved, you can now run your application for development/debug purposes from inside IntelliJ IDEA, without having to deploy to a container or setup 
any application servers.

See also: [Configuration](configuration)

### Use automatic reloading

Ktor can automatically reload application when changes to class files are detected, i.e. when you build the Application.
Enable this feature by adding `watch` configuration to `application.conf`:

```json
ktor {
    deployment {
        port = 8080
        watch = [ my.company ]
    }

    …
}
```

Check [Automatic Reloading](/servers/autoreload.html) article for more details.
