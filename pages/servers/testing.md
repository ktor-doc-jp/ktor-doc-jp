---
title: Testing 
section: Servers
permalink: /application/testing.html
caption: Testing Server Applications 
---

Ktor is designed to allow creating applications that are easily testable. And of course,
Ktor infrastructure itself is well tested with unit, integration and stress tests.
In this section you will learn how to test your applications. 

**Table of contents:**

* TOC
{:toc}

## TestEngine

Ktor has a special kind engine `TestEngine`, that doesn't create a web server, doesn't bind to sockets and doesn't do
any real HTTP requests. Instead, it hooks directly into internal machinery and processes `ApplicationCall` directly. 
This allows for fast test execution at the expense of may be missing some details about HTTP processing. 
It's perfectly capable of testing application logic, but be sure to setup integration tests as well.

Quick walk through:  

* Add `ktor-server-test-host` dependency to `test` scope 
* Create a JUnit test class and a test function
* Use `withTestApplication` function to setup test environment for your Application
* Use `handleRequest` function to send requests to your application and verify results

See an [example](#example) in this page.

## Example

See full example of application testing in [ktor-samples-testable](https://github.com/Kotlin/ktor/tree/master/ktor-samples/ktor-samples-testable) 

**build.gradle:**
```groovy
// ...
dependencies {
    // ...
    testCompile "io.ktor:ktor-server-test-host:$ktor_version"
}
```

**Module:**
```kotlin
fun Application.testableModule() {
    intercept(ApplicationCallPipeline.Call) { call ->
        if (call.request.uri == "/")
            call.respondText("Test String")
    }
}
```

**Test:**
```kotlin
class ApplicationTest {
    @Test fun testRequest() = withTestApplication(Application::testableModule) {
        with(handleRequest(HttpMethod.Get, "/")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("Test String", response.content)
        }
        with(handleRequest(HttpMethod.Get, "/index.html")) {
            assertFalse(requestHandled)
        }
    }
}
```

