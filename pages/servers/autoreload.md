---
title: Autoreload
caption: Saving Time with Automatic Reloading  
section: Servers
permalink: /servers/autoreload.html
---

During development it is important to have fast feedback loop cycle. 
Often, restarting server would take quite some time, so Ktor provides basic auto-reload facility that
reloads just an Application.

**Table of contents:**

* [Using embeddedServer](#embedded-server)
* [Using configuration file](#configuration-file)
* [Example](#example)

<a id="embedded-server"></a>
## Using embeddedServer

When using a custom main using `embeddedServer`, you can use the default parameter `watchPaths` to provide
a list of packages that will be watched and reloaded.

`fun main(args: Array<String>) {
    embeddedServer(
        Netty,
        watchPaths = listOf("io.ktor.exercise.autoreload"),
        port = 8080,
        module = Application::mymodule
    ).apply { start(wait = true) 
}`

Note that here you shouldn't use a lambda to configure the server, but to provide a method reference to your
Application module.

```
fun Application.mymodule() {
    routing {
        get("/plain") {
            call.respondText("Hello World!")
        }
    }
}
```

If yoy try to use a lambda instead of a method reference, you will get the following error:
```
Exception in thread "main" java.lang.RuntimeException: Module function provided as lambda cannot be unlinked for reload
```

To fix this error, you just have to extract your lambda body to an Application extension method (module) just like this:

Code that **won't** work:
```
fun main(args: Array<String>) {
    embeddedServer(Netty, watchPaths = listOf("io.ktor.exercise0"), port = 8080) { // ERROR! Module function provided as lambda cannot be unlinked for reload
        routing {
            get("/") {
                call.respondText("Hello World!")
            }
        }
    }.start(true)
}
```

Code that will work:
```
fun main(args: Array<String>) {
    embeddedServer(Netty, watchPaths = listOf("io.ktor.exercise0"), port = 8080, module = Application::mymodule).start(true) // GOOD!, it will work
}

fun Application.mymodule() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
}
```

<a id="configuration-file"></a>
## Using configuration file

When using a configuration file, for example with a [`DevelopmentEngine`](/servers/engine.html) to either run
from the command line or hosted within another server: 

To enable this feature, add `watch` keys to `ktor.deployment` configuration. 

`watch` - Array of class path entries that should be watched and automatically reloaded.

```
ktor {
    deployment {
        port = 8080
        watch = [ module1, module2 ]
    }
    
    …
}
```

For now watch keys are just strings that are matched with `contains` against classpath entries of the loaded 
application, such as a jar name or a project directory name. 
These classes are then loaded with special `ClassLoader` that is recycled when change is detected.

**Note:** `ktor-server-core` classes are specifically excluded from auto-reloading, so if you are working on something in ktor itself, 
don't expect it to be reload automatically. It can't work because core classes are loaded before auto-reload machinery kicks in. 
The exclusion can potentially be smaller, but it's hard to analyse all the transitive closure of types loaded during
startup.

<a id="example"></a>
## Example

Consider the following example:

You can run the application by either a `build.gradle` or directly within your IDE.
Executing the main method in the example file, or by executing : `io.ktor.server.netty.DevelopmentEngine.main`.
DevelopmentEngine by using `commandLineEnvironment` will be in charge of loading the `application.conf` file (that is in HOCON format).

`Main.kt`:
```kotlin
package io.ktor.exercise.autoreload

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

// Exposed as: `static void io.ktor.exercise.autoreload.MainKt.main(String[] args)`
fun main(args: Array<String>) {
    //io.ktor.server.netty.main(args) // Manually using Netty's DevelopmentEngine
    embeddedServer(Netty, watchPaths = listOf("io.ktor.exercise.autoreload"), port = 8080, module = Application::module).apply { start(wait = true) 
}

// Exposed as: `static void io.ktor.exercise.autoreload.MainKt.module(Application receiver)`
fun Application.module() {
    routing {
        get("/plain") {
            call.respondText("Hello World!")
        }
    }
}
```

`application.conf`:
```kotlin
ktor {
    deployment {
        port = 8080
        watch = [ io.ktor.exercise.autoreload ]
    }

    application {
        modules = [ io.ktor.exercise.autoreload.MainKt.module ]
    }
}
```

As you can see, you specify a list of jvm packages (in this case just `io.ktor.exercise.autoreload`), that should
be reloaded upon modification.

Using gradle, you can run `gradle compile` in another terminal to trigger the compilation.
While using intelliJ you can run `Build -> Build Project` to trigger a recompilation while running.

Try changing the respodText String, trigger a compilation and run a new request in your browser to the see the changes.

Note that it is also possible to watch for sources using gradle, and automatically trigger a Kotlin compilation. 
