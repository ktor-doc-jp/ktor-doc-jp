---
title: Features
keywords: Home Page
tags: [feature]
sidebar: mydoc_sidebar
permalink: features/index.html
summary: Features provide key functionality to your application using a plugin model 
---

A Ktor application typically consists of a series of features. You can think of features as functionality 
that is injected into the request and response pipeline. Usually an application would have a series of features such as `DefaultHeaders` which add headers to every outgoing
response, `Routing` which allows us to define routes to handle requests, etc.

A feature is "installed" into the [Application](/application) using the `install` function

```kotlin
fun Application.main() {
    install(DefaultHeaders) 
    install(CallLogging)
    install(Routing) { 
        get("/") { 
            call.respondText("Hello, World!")  
        }
    }
}
```
Some common feature such as `Routing` come with helper functions, which are defined as extension functions to `Application`, making the code
somewhat more fluent. For instance, instead of writing

```kotlin
    install(Routing) {
        get("/") {
            call.respondText("Hello, World!")
        }
    }
```

we could simply write

```kotlin
    routing {
        get("/") {
            call.respondText("Hello, World!")
        }
    }
```

### Built-in Features

Ktor comes with a number of ready-made features that can be installed into your application:

> Some features might need adding an extra dependency to your project. See feature pages for more details.

#### Application Features

* [Routing](routing): attaches code to specific path/query/method/header and extract parameters from placeholders
* [Locations](locations): provides round-trip bindings of URLs to data classes
* [Sessions](sessions): stores and retrieves additional information attached to client session
* [Authentication](authentication): authenticates client using Basic, Digest, Form, OAuth (1a & 2)
* [Status Pages](status-pages): sends custom content for specific status responses such as 404 Not Found
* [File type mapping](file-mapping): maps file extension to mime type for static file serving
* [Static content](static-content): serves streams of data from local file system with full asynchronous support
* [FreeMarker](freemarker): provides support for Freemarker templates

#### HTTP transport features

* [DefaultHeaders](default-headers): provides default headers with each HTTP response such as Date and Server
* [Compression](compression): enables gzip/deflate compression when client accepts it
* [Conditional Headers](conditional-headers): sends 304 Not Modified response when if-modified-since/etag indicate content is the same
* [Partial Content](partial-content): sends partial content for streaming ranges, like in video streams
* [HEAD response](head-response): responds to HEAD requests by running through pipeline and dropping response body
* [CORS](cors): verifies and sends headers according to cross-origin resource sharing control
* [HSTS and https redirect](hsts): supports strict transport security

### Custom features

You can develop your own features and reuse them across your Ktor applications. 
Refer to [Advanced Features](/advanced/features) for more information.