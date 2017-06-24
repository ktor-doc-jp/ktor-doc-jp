## CORS

Ktor by default provides an interceptor for implementing proper support for Cross-Origin Resource Sharing (CORS).

"Cross-Origin Resource Sharing (CORS) is a specification that enables truly open access across domain-boundaries. If you serve public content, please consider using CORS to open it up for universal JavaScript / browser access." - http://enable-cors.org/

### Basic
First of all, install the CORS feature into your application.
```
fun Application.main() {
  ...
  install(CORS)
  ...
}
```
The default configuration to the CORS feature handles only `GET`, `POST` and `HEAD` HTTP methods and the following headers:
```
  HttpHeaders.CacheControl
  HttpHeaders.ContentLanguage
  HttpHeaders.ContentType
  HttpHeaders.Expires
  HttpHeaders.LastModified
  HttpHeaders.Pragma
```
You can customize those values and more, to learn how, take a look at the Advanced section below.

### Advanced

 - source code:  [here](https://github.com/Kotlin/ktor/blob/master/ktor-core/src/org/jetbrains/ktor/features/CORS.kt)
  - tests :  [here](https://github.com/Kotlin/ktor/blob/master/ktor-core-tests/test/org/jetbrains/ktor/tests/http/CORSTest.kt)

Here is an advanced example that demonstrates most of CORS-related API functions

```
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
### Configs
- method("HTTP_METHOD") : Includes this method to the white list of Http methods to use CORS.
- header : Includes this header to the white list of headers to use CORS.
- anyHost() : Allows any host to access the resources
- host("hostname") : Allows only the specified host to use CORS, it can have the port number, a list of subDomains or the supported schemes.
- allowCredentials : Includes AccessControlAllowCredentials header in the response
- maxAge: Includes AccessControlMaxAge header in the response with the given max age
