---
title: Using Gradle
tags: [overview]
sidebar: mydoc_sidebar
permalink: /getting-started-gradle.html
summary: Explains how to get up and running with Gradle
---

## Basic Kotlin Gradle build file

```groovy
group 'Example'
version '1.0-SNAPSHOT'

buildscript {
    ext.kotlin_version = '{{site.kotlin_version}}'

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}

apply plugin: 'java'
apply plugin: 'kotlin'

sourceCompatibility = 1.8

repositories {
    mavenCentral()
}

dependencies {
    compile "org.jetbrains.kotlin:kotlin-stdlib-jre8:$kotlin_version"
    testCompile group: 'junit', name: 'junit', version: '4.12'
}
```

## Add Ktor dependencies and configure build settings

The Ktor file is located on bintray and it has a dependency on the coroutines library in kotlinx
so we will need to add the following to the repositories block:     

```groovy
 maven { url  "http://dl.bintray.com/kotlin/ktor" }
 maven { url "https://dl.bintray.com/kotlin/kotlinx" }
```

Visit [Bintray](https://bintray.com/kotlin/ktor/ktor) and determine the latest version of ktor.  In this case it is `{{site.ktor_version}}`.  
Then we will designate this as an extra property in the `buildscript` block like:

```groovy
ext.ktor_version = '{{site.ktor_version}}'
```

Now we add `ktor-core` module using `ktor_version` we specified

```groovy
compile "org.jetbrains.ktor:ktor-core:$ktor_version"
```

Coroutines are still an experimental feature in Kotlin, so we will need to tell the compiler that we are okay with using them to avoid warnings:

```groovy
kotlin {
    experimental {
        coroutines "enable"
    }
}
```

We also need to tell Kotlin compiler to generate bytecode compatible with Java 8:

```groovy
compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
```

## Choose your host and configure it

Ktor can run in many environments, such as Netty, Jetty or any Application Server such as Tomcat.
This example shows how to configure Ktor with Netty. For other hosts see [artifacts](artifacts.html) for list of
available modules.

We will add a dependency for ktor-netty using the ktor_version property we created.
This module provides Netty as a web server and all the required code to run Ktor application on top of it

```groovy
compile "org.jetbrains.ktor:ktor-netty:$ktor_version"
```

## Final build.gradle

When you are done the build.gradle file should look like:

```groovy
group 'Example'
version '1.0-SNAPSHOT'

buildscript {
    ext.kotlin_version = '{{site.kotlin_version}}'
    ext.ktor_version = '{{site.ktor_version}}'

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}

apply plugin: 'java'
apply plugin: 'kotlin'

sourceCompatibility = 1.8
compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
}

kotlin {
    experimental {
        coroutines "enable"
    }
}

repositories {
    mavenCentral()
    maven { url "https://dl.bintray.com/kotlin/kotlinx" }
    maven { url "https://dl.bintray.com/kotlin/ktor" }
}

dependencies {
    compile "org.jetbrains.kotlin:kotlin-stdlib-jre8:$kotlin_version"
    compile "org.jetbrains.ktor:ktor-netty:$ktor_version"
    testCompile group: 'junit', name: 'junit', version: '4.12'
}
```

You can now run Gradle to fetch dependencies and verify everything is set up correctly.

## Configure logging

Ktor uses [SLF4J](https://www.slf4j.org/) for logging. If you don't add a logging provider, you will see the
following message when you run your application:

```
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
```

We can set up logging to remove these warning messages and get a better idea of what is happening with the app.

Add the following to the gradle dependencies:

```groovy
compile "ch.qos.logback:logback-classic:1.2.1"
```

Run the app and you should now see the logging messages in the Run pane of IDEA.
However, these logging messages are not as helpful as they could be.  To get a better configuration for logging, create a text file named logback.xml file in the `src/main/resources` directory and put the following xml configuation in it:

```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{YYYY-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="trace">
        <appender-ref ref="STDOUT"/>
    </root>

    <logger name="org.eclipse.jetty" level="INFO"/>
    <logger name="io.netty" level="INFO"/>

</configuration>
```

Stop the app, run it again, and go to localhost:8080 in your browser and now in the IDEA run pane, you should see a log message like:

```
2017-05-29 23:08:12.926 [nettyCallPool-4-1] TRACE ktor.application - 200 OK: GET - /
```

To understand how to change the `logback.xml` configuration file to change the logging, see the [logback manual](https://logback.qos.ch/manual/index.html).
