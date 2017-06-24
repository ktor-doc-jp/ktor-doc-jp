# Getting started with Ktor using Intellij IDEA and Gradle

This tutorial will guide you from the most basic setup to a full featured setup you can use to start developing your app.

## Prerequisites
1.  The most recent version of Intellij IDEA
2.  Kotlin and Gradle plugins enabled (They should be by default.)
Check this in IDEA at:  File -> Settings -> Plugins

## Start a Project
1.  File -> New -> Project
2.  Select Gradle and under Additional Libraries and Frameworks, check Java and Kotlin (Java).  Confirm that Project SDK is completed and click Next.
3.  Enter a GroupId: Example  
and ArtifactId: Example  
and click Next
4.  Check the checkboxes for "Use auto-import", "Create directories for empty content roots automatically", and "Create separate module per source set".  Confirm the Use default gradle wrapper radio button is selected and that Gradle JVM is populated and click Next.
5.  Complete Project name: Example  
and Project location: a/path/on/your/filesystem   
and click Finish
6.  Wait a few seconds for Gradle to run and you should see a project structure like the following (with a few other files and directories):
```
Example 
  src
    main
        java
        kotlin
        resources
    test
        java
        kotlin
        resources
  build.gradle
```

## Set up the Gradle build file
View the build.gradle file.  Initially, it will look like this:

```
group 'Example'
version '1.0-SNAPSHOT'

buildscript {
    ext.kotlin_version = '1.1.2-2'

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

2.  The Ktor file is located on bintray and it has a dependency on the coroutines library in kotlinx 
so we will need to add the following to the repositories block:     
   ```
   maven { url  "http://dl.bintray.com/kotlin/ktor" }
   maven { url "https://dl.bintray.com/kotlin/kotlinx" }
   ``` 

3. Go to [https://bintray.com/kotlin/ktor/ktor] and determine the latest version of ktor.  In this case it is 0.3.2.  
Then we will designate this as an extra property in the buildscript block like:
```
ext.ktor_version = '0.3.2'
```
4.  Then we will add a dependency for ktor-netty using the ktor_version property we created. (This includes the ktor-core.)
```
compile "org.jetbrains.ktor:ktor-netty:$ktor_version"
```
5.  When you are done the build.gradle file should look like:
```
group 'Example'
version '1.0-SNAPSHOT'

buildscript {
    ext.kotlin_version = '1.1.2-2'
    ext.ktor_version = '0.3.2'

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
    maven { url "https://dl.bintray.com/kotlin/kotlinx" }
    maven { url "https://dl.bintray.com/kotlin/ktor" }
}

dependencies {
    compile "org.jetbrains.kotlin:kotlin-stdlib-jre8:$kotlin_version"
    compile "org.jetbrains.ktor:ktor-netty:$ktor_version"
    testCompile group: 'junit', name: 'junit', version: '4.12'
}
```
6.  Gradle will run automatically and pull in these dependencies.
## Create the App

Select the src/main/kotlin directory and create a new package.  We will call it blog.

Select that directory and create a new kotlin file under it named BlogApp

Copy and paste in the most basic setup for an app so that it looks like:

```
package blog

/**
 * Created by You on 5/30/2017.
 */
import org.jetbrains.ktor.netty.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.host.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.response.*

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080) {
        routing {
            get("/") {
                call.respondText("My Example Blog", ContentType.Text.Html)
            }
        }
    }.start(wait = true)
}
```
Now you can Run 'blog.BlogAppKt'  

This will start the Netty web server.
In your browser enter the url:  localhost:8080
And you should see your example blog plage.

### Improve the app with the Application object
The setup above has a lot of nested blocks and is not the best for starting to 
add functionality to your app.  We can improve it by using the Application object 
and referring to that from an embeddedServer call in the main function.  

Change your code in BlogApp.kt to the following to try this:


```
package blog

/**
 * Created by You on 5/30/2017.
 */
import org.jetbrains.ktor.netty.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.features.DefaultHeaders
import org.jetbrains.ktor.host.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.logging.CallLogging
import org.jetbrains.ktor.response.*

fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)
    install(Routing) {
        get("/") {
            call.respondText("My Example Blog2", ContentType.Text.Html)
        }
    }
}

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080, reloadPackages = listOf("BlogAppKt"), module = Application::module).start()
}
```

### Extract out Configuration Data
Although we can designate some application configuration data in the main function embeddedServer call, we can provide more flexibility for future deployments and changes by extracting this out to a separate configuration file.  In the src/main/resources directory we will create a new text file named application.conf with the following content:
```
ktor {
    deployment {
        environment = development
        port = 8080
    }

    application {
        modules = [ blog.BlogAppKt.main ]
    }
}
```
Then we delete the main function from BlogApp.kt and change fun Application.module() to fun Application.main().  However, if we run the application now it will fail with an error message like "Top-level function 'main' not found in package blog."  Our Application.main() function is now a function extension and does not qualify as a top-level main function.   

This requires us to indicate a new main class as IDEA will no longer be able to find it automatically.  In build.gradle we add:
```
apply plugin: 'application'

mainClassName = 'org.jetbrains.ktor.netty.DevelopmentHost'
```

And then go to Run -> Edit Configurations -> select the blog.BlogAppKt configuration and change its Main class to:
org.jetbrains.ktor.netty.DevelopmentHost

Now when we run the new configuration, the application will start again.


## Remove the logger warning messages and set up logging
Each time you run the app you will see the following text in the Run pane:
```
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
```
We can set up logging to remove these warning messages and get a better idea of what is happening with the app.

Add the following to the gradle dependencies:
    
```
    compile "ch.qos.logback:logback-classic:1.2.1"
```
    
Run the app and you should now see the logging messages in the Run pane of IDEA.
However, these logging messages are not as helpful as they could be.  To get a better configuration for logging, create a text file named logback.xml file in the  src/main/resources directory and put the following xml configuation in it:
```
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
    2017-05-29 23:08:12.926 [nettyCallPool-4-1] TRACE ktor.application - 200 OK: GET - /
    
To understand how to change the logback.xml configuration file to change the logging, see the logback manual at:
https://logback.qos.ch/manual/index.html

Other logback documentation is at:
https://logback.qos.ch/documentation.html

