---
title: Default Headers
caption: Send Headers Automatically
section: Features
permalink: /features/default-headers.html
feature:
    artifact: io.ktor:ktor-server-core:$ktor_version
    class: io.ktor.features.DefaultHeaders
---

This feature adds default set of headers to HTTP responses. List of headers is customizable, and `Date` header is cached
to avoid building complex strings on each response.   

{% include feature.html %}

### Usage

```kotlin
fun Application.main() {
  ...
  install(DefaultHeaders)
  ...
}
```

This will add `Date` and `Server` headers to each HTTP response.

### Configuration
 
* `header(name, value)` will add another header to the list of default headers

```kotlin
fun Application.main() {
  ...
  install(DefaultHeaders) {
    header("X-Developer", "John Doe") // will send this header with each response
  }
  ...
}
```

* default `Server` header can be overriden by specifying your custom header:

```kotlin
fun Application.main() {
  ...
  install(DefaultHeaders) {
    header(HttpHeaders.Server, "Konstructor") 
  }
  ...
}
```

* default `Date` header cannot be overriden. If you need to override it, do not install `DefaultHeaders` feature and instead 
intercept the call manually 
