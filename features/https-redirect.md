---
title: HttpsRedirect
caption: Redirect HTTP requests to HTTPS 
category: features
permalink: /features/https-redirect.html
keywords: https ssl
feature:
    artifact: io.ktor:ktor-server-core:$ktor_version
    class: io.ktor.features.HttpsRedirect
---

This feature will make all the affected HTTP calls perform a redirect to its
HTTPS counterpart before processing the call.

By default the redirection is a `301 Moved Permanently`,
but it can be configured to be a `302 Found` redirect.

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

### Testing
{: #testing }

Applying this feature changes how [testing](/servers/testing.html) works.
After applying this feature, each `handleRequest` you perform, results in a redirection response.
And probably this is not what you want in most cases, since that behaviour is already tested.

#### XForwardedHeadersSupport trick

As shown [in this test](https://github.com/ktorio/ktor/blob/bb0765ce00e5746c954fea70270cf7d802a40648/ktor-server/ktor-server-tests/test/io/ktor/tests/server/features/HttpsRedirectFeatureTest.kt#L31-L49){: target="_blank"},
you can install the `XForwardedHeadersSupport` feature and add a `addHeader(HttpHeaders.XForwardedProto, "https")`
header to the request.

```kotlin
@Test
fun testRedirectHttps() {
    withTestApplication {
        application.install(XForwardedHeadersSupport)
        application.install(HttpsRedirect)
        application.routing {
            get("/") {
                call.respond("ok")
            }
        }


        handleRequest(HttpMethod.Get, "/", {
            addHeader(HttpHeaders.XForwardedProto, "https")
        }).let { call ->
            assertEquals(HttpStatusCode.OK, call.response.status())
        }
    }
}
```

#### Do not install the feature when testing or uninstall it

Uninstalling it:

```kotlin
application.uninstall(HttpsRedirect)
```

Prevent installation in the first place:

```kotlin
// The function referenced in the application.conf
fun Application.mymodule() {
    mymoduleConfigured()
}

// The function referenced in the tests
fun Application.mymoduleForTesting() {
    mymoduleConfigured(installHttpsRedirect = false)
}

fun Application.mymoduleConfigured(installHttpsRedirect: Boolean = true) {
    if (installHttpsRedirect) {
        install(HttpsRedirect)
    }
    // ...
}
```

In this case, you can also have a separate test that calls `mymodule` instead of `mymoduleForTesting` to verify that the `HttpsRedirect` feature is installed
and other things that you are not doing in tests.