---
title: Thymeleaf
caption: Using Thymeleaf Templates
category: servers
keywords: html
feature:
  artifact: io.ktor:ktor-thymeleaf:$ktor_version
  class: io.ktor.thymeleaf.Thymeleaf
redirect_from:
- /features/thymeleaf.html
- /features/templates/thymeleaf.html
ktor_version_review: 1.2.0
---

Ktor includes support for [Thymeleaf](https://www.thymeleaf.org/) templates through the Thymeleaf
feature.  Initialize the Thymeleaf feature with a
[ClassLoaderTemplateResolver](https://www.thymeleaf.org/apidocs/thymeleaf/3.0.1.RELEASE/org/thymeleaf/templateresolver/ClassLoaderTemplateResolver.html):

```kotlin
    install(Thymeleaf) {
        setTemplateResolver(ClassLoaderTemplateResolver().apply { 
            prefix = "templates/"
            suffix = ".html"
            characterEncoding = "utf-8"
        })
    }
```

This TemplateResolver sets up Thymeleaf to look for the template files on the classpath in the
"templates" package, relative to the current class path.  A basic template looks like this:

{% include feature.html %}

```html
<!DOCTYPE html >
<html xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8">
  <title>Title</title>
</head>
<body>
<span th:text="${user.name}"></span>
</body>
</html>
```

With that template in `resources/templates` it is accessible elsewhere in the the application
using the `call.respond()` method:

```kotlin
    get("/") {
        call.respond(ThymeleafContent("index", mapOf("user" to User(1, "user1"))))
    }
```
