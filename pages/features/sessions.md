---
title: Sessions
caption: Handle Conversations with Sessions
section: Features
permalink: /features/sessions.html
---

Sessions are means of establishing conversational context into otherwise stateless HTTP nature. 
They allow servers to keep a piece of information associated with the client during a sequence of HTTP requests and responses. 
Different use-cases include authentication and authorization, user tracking, keeping information at client like a shopping cart, 
and more. Sessions are typically implemented by employing `Cookies`, but could also be done using headers. Sessions are either
stateless when entire data object goes back and force between client and server, or as server-side storage when only session ID 
is traveling back and forth and associated data is retrieved at server. 

### Usage

```kotlin
application.install(Sessions) {
    cookie<MySession>("SESSION")
} 
```

This installs `Sessions` feature and maps cookie with the name `"SESSION"` to `MySession` type in stateless way. 
Entire class `MySession` will be serialised into a string and send to client as a cookie. When client makes another request,
the cookie is deserialized back into `MySession` and is available to server code:

```kotlin
application.routing {
    get("/") {
        val mySession = call.sessions.get<MySession>()
    }
}
```

If session was not set the returned value will be null.

To create or modify current session you just call a `set` function on a `sessions` property with the value of the
data class: 

```kotlin
call.sessions.set(MySession(name = "John", value = 12))
```

When user logs out or session should be cleared for some other reason, you call `clear` function:

```kotlin
call.sessions.clear<MySession>()
```

Since there could be several conversational states for a single application, you can install multiple session mappings:

```kotlin
application.install(Sessions) {
    cookie<Session1>("Session1") // install a cookie stateless session
    header<Session2>("Session2", sessionStorage) { // install a header server-side session
        transform(SessionTransportTransformerDigest()) // sign the ID that travels to client
    }
}
``` 

For multiple session mapping, both type and name should be unique. 

### Configuration

Since sessions can be implemented by various techniques, there is an extensive configuration facility to set them up:

* `cookie` will install cookie-based transport
* `header` will install header-based transport 

Each of these functions will get the name of the cookie or header. 

If the function is passed an argument of type `SessionStorage` it will use the storage to save the session, otherwise
it will serialize data into the cookie/header value.

Each of these functions can receive an optional configuration lambda.

For cookies, the receiver is `CookieSessionBuilder` which allows to:

* specify custom `serializer`
* add a value `transformer`, like signing or encrypting
* specify cookie configuration such as duration, encoding, domain, path and so on

For headers, the receiver is `HeaderSessionBuilder` that allows `serializer` and `transformer` customization.

For cookies & headers that are server-side with a `SessionStorage`, additional configuration is `identity` function
that should generate a new ID when the new session is created. 

