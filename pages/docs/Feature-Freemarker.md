---
title: Freemarker Templates
keywords: Home Page
tags: [feature]
sidebar: mydoc_sidebar
permalink: /features/freemarker.html
summary: Adds support for Freemarker templates
---

Ktor includes support for [FreeMarker](http://freemarker.org/) templates through the FreeMarker
feature.  Initialize the FreeMarker feature with a
[TemplateLoader](http://freemarker.org/docs/pgui_config_templateloading.html):

```kotlin
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(TheApp::class.java.classLoader, "templates")
    }
```

This TemplateLoader sets up FreeMarker to look for the template files on the classpath in the
"templates" package, relative to the current class path.  A basic template looks like this:

```html
<html>
<h2>Hello ${user.name}!</h2>

Your email address is ${user.email}
</html>
```

With that template in `resources/templates` it is accessible elsewhere in the the application
using the `call.respond()` method:

```kotlin
    get("/{...}") {
        val user = User("user name", "user@example.com")
        call.respond(FreemarkerContent("index.ftl", mapOf("user" to user), "e"))
    }
```