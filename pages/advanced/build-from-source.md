---
title: Building Ktor
caption: Building Ktor From Source  
section: Advanced
keywords: git compiling compile ktor from source
permalink: /advanced/building-from-source.html
---

Ktor is an OpenSource project hosted at github:
<https://github.com/ktorio/ktor>

We usually provide binary version previews at bintray:
<https://bintray.com/kotlin/ktor/ktor>

In addition, you can use jitpack to get bleeding edge artifacts compiled from master:
<https://jitpack.io/#ktorio/ktor>

## Downloading the sources
{: #get-git-sources}

You can get the lastest version of Ktor using GIT to clone Ktor's repository:

```
git clone https://github.com/ktorio/ktor.git
cd ktor
```

## Building
{: #building}

Ktor uses gradle for building. It should work with any gradle version
greater than 4.3, but for best results we provide a gradle wrapper,
that should work with any supported system just having a JDK installed: 

```
./gradlew build
```

Right now Ktor doesn't compile with Java 9. So for now, you should stick to Java 8.
{: .note }

## Installing locally
{: #installing}

Ktor provides a gradle install task, that installs ktor artifacts in your
local maven repository:

```
./gradlew install
```

## Troubleshooting
{: #troubleshooting }

If you get an error similar to:

```
* Where:
Build file '/.../ktor/ktor-server/ktor-server-benchmarks/build.gradle' line: 2

* What went wrong:
An exception occurred applying plugin request [id: 'me.champeau.gradle.jmh', version: '0.4.4']
> Failed to apply plugin [id 'me.champeau.gradle.jmh']
   > Could not generate a proxy class for class me.champeau.gradle.JMHPluginExtension.
```

You might have forgotten to use the gradle wrapper (`./gradlew`) and your default installed
gradle version is lower than 4.3.

To ensure that it works, please using the gradle wrapper instead.
