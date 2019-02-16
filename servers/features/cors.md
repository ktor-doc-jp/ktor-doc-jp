---
title: CORS
caption: Enable Cross-Origin Resource Sharing (CORS)
category: servers
permalink: /servers/features/cors.html
feature:
  artifact: io.ktor:ktor-server-core:$ktor_version
  class: io.ktor.features.CORS
redirect_from:
- /features/cors.html
ktor_version_review: 1.0.0
---

Ktor by default provides an interceptor for implementing proper support for Cross-Origin Resource Sharing (CORS).

> Cross-Origin Resource Sharing (CORS) is a specification that enables truly open access across domain-boundaries. If you serve public content, please consider using CORS to open it up for universal JavaScript / browser access. 
*From [enable-cors.org](http://enable-cors.org/)*

{% include feature.html %}

## Basic

First of all, install the CORS feature into your application.

```kotlin
fun Application.main() {
  ...
  install(CORS)
  ...
}
```

The default configuration to the CORS feature handles only `GET`, `POST` and `HEAD` HTTP methods and the following headers:

```kotlin
  HttpHeaders.CacheControl
  HttpHeaders.ContentLanguage
  HttpHeaders.ContentType
  HttpHeaders.Expires
  HttpHeaders.LastModified
  HttpHeaders.Pragma
```

## Advanced

 - [source code](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-core/jvm/src/io/ktor/features/CORS.kt)
 - [tests](https://github.com/ktorio/ktor/blob/master/ktor-server/ktor-server-tests/test/io/ktor/tests/http/CORSTest.kt)

Here is an advanced example that demonstrates most of CORS-related API functions

```kotlin
fun Application.main() {
  ...
  install(CORS)
  {
    method(HttpMethod.Options)
    header(HttpHeaders.XForwardedProto)
    anyHost()
    host("my-host")
    // host("my-host:80")
    // host("my-host", subDomains = listOf("www"))
    // host("my-host", schemes = listOf("http", "https"))
    allowCredentials = true
    maxAge = Duration.ofDays(1)

  }
  ...
}
```

## Configuration

- `method("HTTP_METHOD")` : Includes this method to the white list of Http methods to use CORS.
- `header("header-name")` : Includes this header to the white list of headers to use CORS.
- `anyHost()` : Allows any host to access the resources
- `host("hostname")` : Allows only the specified host to use CORS, it can have the port number, a list of subDomains or the supported schemes.
- `allowCredentials` : Includes AccessControlAllowCredentials header in the response
- `maxAge`: Includes AccessControlMaxAge header in the response with the given max age
