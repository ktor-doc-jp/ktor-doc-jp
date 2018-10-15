---
title: Gradle
caption: Setting up a Gradle Build
category: quickstart
toc: true
permalink: /quickstart/quickstart/gradle.html
redirect_from:
  - /quickstart/gradle.html
priority: 0
---

In this guide, we will show you how to create a `build.gradle` file
and how to configure it to support Ktor.

**Table of contents:**

* TOC
{:toc}

## Basic Kotlin `build.gradle` file (without Ktor)
{: #initial }

First of all, you need a skeleton `build.gradle` file including Kotlin.
You can create it with any text editor, or you can use IntelliJ to create
it following the [IntelliJ guide](/quickstart/quickstart/intellij-idea.html).

The initial file looks like this:

```groovy
group 'Example'
version '1.0-SNAPSHOT'

buildscript {
    ext.kotlin_version = '{{site.kotlin_version}}'

    repositories {
        mavenCentral()
        maven { url "https://dl.bintray.com/kotlin/kotlin-eap" }
    }
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}

apply plugin: 'java'
apply plugin: 'kotlin'

sourceCompatibility = 1.8

repositories {
    jcenter()
    maven { url "https://dl.bintray.com/kotlin/kotlin-eap" }
}

dependencies {
    compile "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
    testCompile group: 'junit', name: 'junit', version: '4.12'
}
```
{: .compact}

## Add Ktor dependencies and configure build settings
{: #ktor-dependencies}

Ktor artifacts are located in a specific repository on bintray.
And its core has dependencies on the `kotlinx.coroutines` library that
can be found on `jcenter`.

You have to add both to the `repositories` block in the `build.gradle` file:

```groovy
jcenter()
maven { url "https://dl.bintray.com/kotlin/ktor" }
```

Visit [Bintray](https://bintray.com/kotlin/ktor/ktor) and determine the latest version of ktor.
In this case it is `{{site.ktor_version}}`.

You have to specify that version in each Ktor artifact reference,
and to avoid repetitions, you can specify that version in an extra property
in the `buildscript` block (or in a `gradle.properties` file) for using it later:

```groovy
ext.ktor_version = '{{site.ktor_version}}'
```

Now you have to add the `ktor-server-core` artifact, referencing the `ktor_version` you specified:

```groovy
compile "io.ktor:ktor-server-core:$ktor_version"
```

In groovy, there are single-quoted strings (instead of characters)
and double-quoted strings, to be able to interpolate variables like
versions, you have to use double-quoted strings.
{: .note.tip }

As for Kotlin 1.2x, coroutines are still an experimental feature, 
so you will need to tell the compiler that it is okay
to use them to avoid warnings:

```groovy
kotlin {
    experimental {
        coroutines "enable"
    }
}
```

You also need to tell the Kotlin compiler to generate bytecode
compatible with Java 8:
{: #java8}

```groovy
compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
```

## Choose your engine and configure it
{: #engine}

Ktor can run in many environments, such as Netty, Jetty or any other
Servlet-compatible Application Container such as Tomcat.

This example shows you how to configure Ktor with Netty.
For other engines see [artifacts](/quickstart/artifacts.html) for a list of
available artifacts.

You will add a dependency for `ktor-server-netty` using the
`ktor_version` property you have created. This module provides
a Netty web server and all the required code to run Ktor
application on top of it:

```groovy
compile "io.ktor:ktor-server-netty:$ktor_version"
```

## Final `build.gradle` (with Ktor)
{: #complete}

When you are done, the `build.gradle` file should look like this:

```groovy
group 'Example'
version '1.0-SNAPSHOT'

buildscript {
    ext.kotlin_version = '{{site.kotlin_version}}'
    ext.ktor_version = '{{site.ktor_version}}'

    repositories {
        mavenCentral()
        maven { url "https://dl.bintray.com/kotlin/kotlin-eap" }
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
    jcenter()
    maven { url "https://dl.bintray.com/kotlin/ktor" }
    maven { url "https://dl.bintray.com/kotlin/kotlin-eap" }
}

dependencies {
    compile "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
    compile "io.ktor:ktor-server-netty:$ktor_version"
    testCompile group: 'junit', name: 'junit', version: '4.12'
}
```

You can now run Gradle (just `gradle` or `./gradlew` if using the wrapper)
to fetch dependencies and verify everything is set up correctly.

## Configure logging
{: #logging}

If you want to log application events and useful information,
you can read about it further in the [logging](/servers/logging.html) page.
