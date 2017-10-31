---
title: Maven
caption: Setting up Maven Build
section: Quick Start
permalink: /quickstart/maven.html
---

Maven is a build automation tool used primarily for Java projects. It reads project configuration from `pom.xml` files. 
Here is basic `pom.xml` file for building Kotlin applications:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>org.jetbrains</groupId>
    <artifactId>sample</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>org.jetbrains sample</name>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <kotlin.version>{{site.kotlin_version}}</kotlin.version>
        <junit.version>4.12</junit.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-stdlib</artifactId>
            <version>${kotlin.version}</version>
        </dependency>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-test-junit</artifactId>
            <version>${kotlin.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <sourceDirectory>src/main/kotlin</sourceDirectory>
        <testSourceDirectory>src/test/kotlin</testSourceDirectory>

        <plugins>
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
                <version>${kotlin.version}</version>
                <executions>
                    <execution>
                        <id>compile</id>
                        <phase>compile</phase>
                        <goals>
                            <goal>compile</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>test-compile</id>
                        <phase>test-compile</phase>
                        <goals>
                            <goal>test-compile</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

### Add Ktor dependencies and configure build settings

The Ktor file is located on bintray and it has a dependency on the coroutines library in kotlinx 
so we will need to add the following to the repositories block:     
   
```xml
    <repositories>
        <repository>
            <id>ktor</id>
            <url>http://dl.bintray.com/kotlin/ktor</url>
        </repository>
        <repository>
            <id>jcenter</id>
            <url>http://jcenter.bintray.com</url>
        </repository>
    </repositories>
``` 

Visit [Bintray](https://bintray.com/kotlin/ktor/ktor) and determine the latest version of ktor.  In this case it is `{{site.ktor_version}}`.  
Then we will designate this as an extra property in the properties block like this:

```xml
    <properties>
        <ktor.version>{{site.ktor_version}}</ktor.version>
    </properties>
```

Now we add `ktor-server-core` module using `ktor.version` we specified
 
```xml
        <dependency>
            <groupId>io.ktor</groupId>
            <artifactId>ktor-server-core</artifactId>
            <version>${ktor.version}</version>
        </dependency>
```

Coroutines are still experimental feature in Kotlin programming language, so we will need to tell compiler we
are okay with using them to avoid warnings. We also need to tell Kotlin compiler to generate bytecode compatible with Java 8:


```xml
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                
                …
                
                <configuration>
                    <jvmTarget>1.8</jvmTarget>
                    <args>
                        <arg>-Xcoroutines=enable</arg>
                    </args>
                </configuration>
```

### Choose your engine and configure it

Ktor can run in many environments, such as Netty, Jetty or any Application Server such as Tomcat. 
This example shows how to configure Ktor with Netty. For other engines see [artifacts](artifacts.html) for list of
available modules.

We will add a dependency for `ktor-server-netty` using the ktor_version property we created. 
This module provides Netty as a web server and all the required code to run Ktor application on top of it

```xml
        <dependency>
            <groupId>io.ktor</groupId>
            <artifactId>ktor-server-netty</artifactId>
            <version>${ktor.version}</version>
        </dependency>
```

### Final pom.xml

When you are done the pom.xml file should look like:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>org.jetbrains</groupId>
    <artifactId>sample</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>org.jetbrains sample</name>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <kotlin.version>{{site.kotlin_version}}</kotlin.version>
        <ktor.version>{{site.ktor_version}}</ktor.version>
        <junit.version>4.12</junit.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-stdlib</artifactId>
            <version>${kotlin.version}</version>
        </dependency>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-test-junit</artifactId>
            <version>${kotlin.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>io.ktor</groupId>
            <artifactId>ktor-server-netty</artifactId>
            <version>${ktor.version}</version>
        </dependency>

    </dependencies>

    <build>
        <sourceDirectory>src/main/kotlin</sourceDirectory>
        <testSourceDirectory>src/test/kotlin</testSourceDirectory>

        <plugins>
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
                <version>${kotlin.version}</version>
                <executions>
                    <execution>
                        <id>compile</id>
                        <phase>compile</phase>
                        <goals>
                            <goal>compile</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>test-compile</id>
                        <phase>test-compile</phase>
                        <goals>
                            <goal>test-compile</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <jvmTarget>1.8</jvmTarget>
                    <args>
                        <arg>-Xcoroutines=enable</arg>
                    </args>
                </configuration>
            </plugin>
        </plugins>
    </build>

    <repositories>
        <repository>
            <id>ktor</id>
            <url>http://dl.bintray.com/kotlin/ktor</url>
        </repository>
        <repository>
            <id>jcenter</id>
            <url>http://jcenter.bintray.com</url>
        </repository>
    </repositories>
</project>
```

You can now run `mvn package` to fetch dependencies and verify everything is set up correctly.

### Configure logging

Ktor uses [SLF4J](https://www.slf4j.org/) for logging. If you don't add a logging provider, you will see the 
following message when you run your application:

```
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
```

We can set up logging to remove these warning messages and get a better idea of what is happening with the app.

Add the following to the dependencies:
    
```xml
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>1.2.1</version>
        </dependency>
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


