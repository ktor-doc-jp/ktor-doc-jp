---
title: Testing Applications 
keywords: Home Page
tags: [overview]
sidebar: mydoc_sidebar
permalink: application/testing.html
summary: 
---

Ktor has special kind of hosting, `TestHost` that doesn't create a web server, doesn't bind to sockets and doesn't do
any real HTTP requests. Instead, it hooks directly into internal machinery and processes `ApplicationCall` directly. 
This allows for fast test execution at the expense of may be missing some details about HTTP processing. 
It's perfectly capable of testing application logic, but be sure to setup integration tests as well.

Quick walk through:  

* Add `ktor-test-host` dependency to `test` scope 
* Create a JUnit test class and a test function
* Use `withTestApplication` function to setup test environment for your Application
* Use `handleRequest` function to send requests to your application and verify results

See full example of application testing in [ktor-samples-testable](https://github.com/Kotlin/ktor/tree/master/ktor-samples/ktor-samples-testable) 

### Sample application

```kotlin
fun Application.testableApplication() {
    intercept(ApplicationCallPipeline.Call) { call ->
        if (call.request.uri == "/")
            call.respondText("Test String")
    }
}
```

### Application test
```kotlin
class ApplicationTest {
    @Test fun testRequest() = withTestApplication(Application::testableApplication) {
        with (handleRequest(HttpMethod.Get, "/")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("Test String", response.content)
        }
        with (handleRequest(HttpMethod.Get, "/index.html")) {
            assertFalse(requestHandled)
        }
    }
}
```

