---
title: Structure
caption: Building Complex Servers 
section: Servers
permalink: /servers/structure.html
---

Depending on the complexity of the code of your server, you might want to structure your code
one way or another. This page proposes some strategies to structure your code according to its
complexity, adapting to its growth, while keeping it as simple as possible.

**Table of contents:**

* TOC
{:toc}

## Hello World

To get started with Ktor, you can start with an `embeddedServer` in a simple `main` function.

```kotlin
fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) {
        routing {
            get("/") {
                call.respondText("Hello World!")
            }
        }
    }.start(wait = true)
}
```

This works fine to understand how Ktor works and to have all the application code available
at a glance.

## Extracting routes

Once your code starts to grow and you have more routes defined, you will probably want to split
that code instead of growing that main function indefinitely. A simple way to do this, is to extract routes into extension methods.
Depending on the size, maybe still in the same file.

```kotlin
fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) {
        routing {
            get("/") {
                call.respondText("Hello World!")
            }
        }
    }.start(wait = true)
}
```

## Deployment and `application.conf`

Once you want to deploy your server. 
