### Testing applications

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

