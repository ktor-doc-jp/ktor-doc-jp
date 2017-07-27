---
title: Authentication
tags: [feature]
permalink: drafts/features/authentication.html
---

Authentication feature provides support for various authentication methods used for web applications. It has pluggable
architecture and allows building your own authentication procedures. Out of the box, it supports Basic, Digest, Form and
OAuth1a and OAuth2 methods. 

### Usage

```kotlin
fun Application.main() {
  install(Authentication) {
     // authentication providers configuration
  }
}
```

There is also a shortcut method:

```kotlin
fun Application.main() {
  authentication {
    ...
  }
}
```

Authentication can be installed not only on application level, but into any ApplicationCallPipeline, such as routing:

```kotlin
fun Application.main() {
  routing {
    ...
    route("members") {
        authentication { 
            ...
        }
    }
  }
}
```

If authentication succeeds, `call.principal<TPrincipal>()` further in call handling will return non-null value 
for the currently logged principal.
`TPrincipal` is a type implementing `Principal`. 

For information about developing custom authentication procedures see [Advanced Authentication](/advanced/authentication)

### Basic Authentication

```kotlin
authentication {
    basicAuthentication("realm") { credentials ->
        if (validate(credentials)) UserIdPrincipal(credentials.name) else null
    }
}
```

`basicAuthentication` checks `Authorization` header for `Basic` schema and loads specified `credentials`. 
It then passes `UserPasswordCredential` instance into provided lambda for validation. 

If instance of `Principal` was returned, normally a `UserIdPrincipal`, it is assumed that a log in was successful. 

If null is returned, or if there were no `Authorization` header, or it was broken, 
then `401 Unauthorized` response is sent back. Normally browser will open a username/password window and resend a request
with credentials provided. 

### Digest Authentication

```kotlin
authentication {
    val p = "Circle Of Life"
    val digester = MessageDigest.getInstance("MD5")
    digestAuthentication("realm") { userName, realm -> digest(digester, "$userName:$realm:$p") }
}
```

`digestAuthentication` checks `Authorization` header for `Digest` schema and verifies it. If verification passes,
it assumes authentication to be successful and assign `UserIdPrincipal` as current principal. 

> TODO: Explain in more details how Digest works, I have no idea.

### Form Authentication

```kotlin
authentication {
    formAuthentication("userid-parameter", "password-parameter",
            challenge = FormAuthChallenge.Redirect { call, credentials -> call.url("/login") },
            validate = { credential -> validate(credential) })
}
```

`formAuthentication` retrieves userid-parameter and password-parameter from values sent by a form POST, and validates
them using provided validation function. If validation function returned a non-null `Prinicpal`, it assumes successful
authentication and given principal is stored as currently logged in. 

If validation fails, or post parameters are missing, authentication fails and specified challenge is executed:

* `FormAuthChallenge.Redirect`: redirection response to the specified url is sent
* `FormAuthChallenge.Unauthorized`: 401 Unauthorized response is sent


### OAuth Authentication

> TODO


 