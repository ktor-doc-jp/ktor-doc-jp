---
title: HttpsRedirect
caption: Redirect HTTP requests to HTTPS 
section: Features
permalink: /features/https-redirect.html
keywords: https ssl
feature:
    artifact: io.ktor:ktor-server-core:$ktor_version
    class: io.ktor.features.HttpsRedirect
---

This feature will make all the affected http calls to perform a redirect to its
https counterpart before processing the call.

By default the redirection is a `301 Moved Permanently`,
but can be configured to be a `302 Found` redirect.

{% include feature.html %}

### Usage

```kotlin
fun Application.main() {
    install(HttpsRedirect) 
}
```

The code above installs the HttpsRedirect feature with the default configuration.  

### Configuration

```kotlin
fun Application.main() {
    install(HttpsRedirect) {
        // The port to redirect to. By default 443, the default HTTPS port. 
        sslPort = 443
        // 301 Moved Permanently, or 302 Found redirect.
        permanentRedirect = true
    }
}
```
