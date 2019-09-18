---
title: Fat JAR
caption: Fat JAR
category: servers
permalink: /servers/deploy/packing/fatjar.html
ktor_version_review: 1.0.0
---

*Fat JAR*（または *uber-jar* とも呼ばれる）アーカイブは通常、すべての依存ライブラリを１つにまとめた単一のJARファイルで、
Javaを使ってスタンドアロンのアプリケーションとして起動することができます。

`java -jar yourapplication.jar`

これは[docker](/servers/deploy/containers.html#docker)のようなコンテナ内で起動する場合や、
[heroku](/servers/deploy/hosting/heroku.html)にデプロイする場合や、
[nginx](/servers/deploy/containers.html#nginx)をリバースプロキシとして置く場合に好ましい方法です。

## Gradle
{: #fat-jar-gradle}

Gradleを利用する際に、[`shadow`](https://imperceptiblethoughts.com/shadow/) gradleプラグインをJARを生成するために利用できます。
例えば、Nettyをエンジンとして利用するFatJarの生成は以下のようになります。

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

// This task will generate your fat JAR and put it in the ./build/libs/ directory
shadowJar {
    manifest {
        attributes 'Main-Class': mainClassName
    }
}
```
{% endcapture %}

{% capture build-gradle-kts %}
```kotlin
plugins {
    application
    kotlin("jvm") version "1.3.21"
    // Shadow 5.0.0 requires Gradle 5+. Check the shadow plugin manual if you're using an older version of Gradle.
    id("com.github.johnrengelman.shadow") version "5.0.0"
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

tasks.withType<Jar> {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to application.mainClassName
            )
        )
    }
}
```
{% endcapture %}

{% include tabbed-code.html
    tab1-title="build.gradle" tab1-content=build-gradle
    tab2-title="build.gradle.kts" tab2-content=build-gradle-kts
    no-height="true"
%}

## Maven
{: #fat-jar-maven}

Mavenを使っている場合は、`maven-assembly-plugin`を使うことでFatJARアーカイブを生成できます。
例えば、Nettyをエンジンとして利用するFatJARの生成方法は次のとおりです。

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
