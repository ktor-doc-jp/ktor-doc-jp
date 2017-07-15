---
title: Static Content
tags: [feature]
permalink: drafts/features/static-content.html
---
      
Static content provides simple way of handling a number of routes that serve static files, like css, js

Folders are relative to current working directory, unless staticRootFolder is specified      
      
```kotlin
application.routing {
    static("static") { // serve files at /static route
        files("css") // serve all files from css folder
        files("js") // serve all files from js folder
        file("image.png") // serve image.png from current folder
    }
```
* /static/foo.css from css/foo.css
* /static/bar.js from js/bar.js
* /static/index.html from image.png


```kotlin
    static("selected") {
        staticRootFolder = basedir
        files("http")
        file("routing/RoutingBuildTest.kt")
        route("virtual") {
            default("http/CodecTest.kt")
            file("foobar.kt", "routing/RoutingBuildTest.kt")
        }
    }
```

Files are served from `basedir` instead of current working directory.

* files from `http` folder are served at `/selected`
* file `routing/RoutingBuildTest.kt` is served at `/selected/routing/RoutingBuildTest.kt`
* `virtual` is an additional route, so all nested files are served at `/selected/virtual`
* if no file is specified for `/selected/virtual` then `http/CodecTest.kt` is served
* when requesting `/selected/virtual/foobar.kt` file `routing/RoutingBuildTest.kt` will be served

ContentType of the served content is determined from the file extension, using `ContentType.defaultForFile(file)`
For this `mimelist.csv` resource file in `ktor-core` is being used.

If file is not found, `FileNotFoundException` is thrown. It should be handled in `StatusPages` with `exception` handler 
to produce 404 Not Found, otherwise it propagates to the host and causes 500 Internal Server Error. 
 