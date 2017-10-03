---
title: Default Headers
tags: [feature]
sidebar: mydoc_sidebar
permalink: /features/default-headers.html
---

This feature adds default set of headers to HTTP responses. List of headers is customizable, and `Date` header is cached
to avoid building complex strings on each response.   

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
