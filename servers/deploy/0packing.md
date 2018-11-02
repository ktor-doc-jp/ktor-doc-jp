---
title: Packing
caption: Packing  
category: servers
permalink: /servers/deploy/packing.html
---

When deploying, normally you will want to generate a single archive with all your
classes, dependencies, and resources packed together: either in a single JAR archive
(also called Fat JAR) or a WAR file (Web Application Resource).

### Fat JAR (Standalone)
{: #fat-jar}

A *fat-jar* (or *uber-jar*) archive allows you to generate a single archive to run your standalone
embedded application directly using Java: `java -jar yourapplication.jar`.

This is the preferred way for running it in a container like [docker](#docker), when deploying to [heroku](#heroku)
or when being reverse-proxied with [nginx](#nginx). 

#### Gradle
{: #fat-jar-gradle}

When using Gradle, you can use the `shadow` gradle plugin to generate it. For example,
to generate a fat JAR using netty as an engine:

```groovy
buildscript {
    repositories {
        jcenter()
        maven { url "https://dl.bintray.com/kotlin/kotlin-eap" }
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
{: .compact }

#### Maven
{: #fat-jar-maven}

When using Maven, you can generate a fat JAR archive with the `maven-assembly-plugin`. For example, to generate
a fat JAR using netty as an engine:

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
{: .compact }

### WAR (Servlet Container)
{: #war}

A WAR archive allows you to easily deploy your application inside your web container / servlet container,
by just copying it to its `webapps` folder. Ktor supports two popular servlet containers: [Jetty](#jetty) and [Tomcat](#tomcat).
It also serves when deploying to [google app engine](#google-app-engine).

To generate a war file, you can use the gretty gradle plugin. You also need a `WEB-INF/web.xml` which looks like this:

`webapp/WEB-INF/web.xml`:
```xml
<?xml version="1.0" encoding="ISO-8859-1" ?>

<web-app xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
         version="3.0">
    <!-- path to application.conf file, required -->
    <!-- note that this file is always loaded as an absolute path from the classpath -->
    <context-param>
        <param-name>io.ktor.ktor.config</param-name>
        <param-value>application.conf</param-value>
    </context-param>
	
    <servlet>
        <display-name>KtorServlet</display-name>
        <servlet-name>KtorServlet</servlet-name>
        <servlet-class>io.ktor.server.servlet.ServletApplicationEngine</servlet-class>

        <!-- required! -->
        <async-supported>true</async-supported>

        <!-- 100mb max file upload, optional -->
        <multipart-config>
            <max-file-size>304857600</max-file-size>
            <max-request-size>304857600</max-request-size>
            <file-size-threshold>0</file-size-threshold>
        </multipart-config>
    </servlet>

    <servlet-mapping>
        <servlet-name>KtorServlet</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>

</web-app>
```
{: .compact}

`build.gradle`:
```groovy
buildscript {
    ext.gretty_version = '2.0.0'

    repositories {
        jcenter()
        maven { url "https://dl.bintray.com/kotlin/kotlin-eap" }
    }
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        classpath "org.akhikhl.gretty:gretty:$gretty_version"
    }
}

apply plugin: 'kotlin'
apply plugin: 'war'
apply plugin: 'org.akhikhl.gretty'

webAppDirName = 'webapp'

gretty {
    contextPath = '/'
    logbackConfigFile = 'resources/logback.xml'
}

sourceSets {
    main.kotlin.srcDirs = [ 'src' ]
    main.resources.srcDirs = [ 'resources' ]
}

repositories {
    jcenter()
    maven { url "http://kotlin.bintray.com/ktor" }
    maven { url "https://dl.bintray.com/kotlin/kotlin-eap" }
}

dependencies {
    compile "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
    compile "io.ktor:ktor-server-servlet:$ktor_version"
    compile "io.ktor:ktor-html-builder:$ktor_version"
    compile "ch.qos.logback:logback-classic:$logback_version"
}

kotlin.experimental.coroutines = 'enable'

task run

afterEvaluate {
    run.dependsOn(tasks.findByName("appRun"))
}
```
{: .compact}

This gradle buildscript defines [several tasks](http://akhikhl.github.io/gretty-doc/Gretty-tasks) that
you can use to run your application.

In the case where you only need to generate a war file, there is a `war` task defined in the war plugin.<br />
Just run `./gradlew war` and it will generate a `/build/libs/projectname.war` file.
{: .note #generate-war-file }

For a full example: <https://github.com/ktorio/ktor-samples/tree/master/deployment/jetty-war>
{: .note.example}

