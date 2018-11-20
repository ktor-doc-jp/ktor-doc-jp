---
title: Fat JAR
caption: Fat JAR
category: servers
permalink: /servers/deploy/packing/fatjar.html
ktor_version_review: 1.0.0
---

A *fat-jar* (or *uber-jar*) archive is a normal jar file single archive packing all the dependencies together
so it can be run a standalone application directly using Java:

`java -jar yourapplication.jar`

This is the preferred way for running it in a container like [docker](/servers/deploy/containers.html#docker), when deploying to [heroku](/servers/deploy/hosting/heroku.html)
or when being reverse-proxied with [nginx](/servers/deploy/containers.html#nginx). 

## Gradle
{: #fat-jar-gradle}

When using Gradle, you can use the `shadow` gradle plugin to generate it. For example,
to generate a fat JAR using netty as an engine:

{% capture build-gradle %}
```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.github.jengelman.gradle.plugins:shadow:2.0.4'
    }
}

apply plugin: 'com.github.johnrengelman.shadow'
apply plugin: 'kotlin'
apply plugin: 'application'

//mainClassName = 'io.ktor.server.netty.DevelopmentEngine' // For versions < 1.0.0-beta-3
mainClassName = 'io.ktor.server.netty.EngineMain' // Starting with 1.0.0-beta-3
```
{% endcapture %}

{% include tabbed-code.html
    tab1-title="build.gradle" tab1-content=build-gradle
    no-height="true"
%}

## Maven
{: #fat-jar-maven}

When using Maven, you can generate a fat JAR archive with the `maven-assembly-plugin`. For example, to generate
a fat JAR using netty as an engine:

{% capture pom-xml %}
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-assembly-plugin</artifactId>
    <version>3.1.0</version>
    <configuration>
        <descriptorRefs>
            <descriptorRef>jar-with-dependencies</descriptorRef>
        </descriptorRefs>
        <archive>
            <manifest>
                <addClasspath>true</addClasspath>
                <mainClass>io.ktor.server.netty.EngineMain</mainClass>
            </manifest>
        </archive>
    </configuration>
    <executions>
        <execution>
            <id>assemble-all</id>
            <phase>package</phase>
            <goals>
                <goal>single</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
{% endcapture %}

{% include tabbed-code.html
    tab1-title="pom.xml" tab1-content=pom-xml
    no-height="true"
%}
