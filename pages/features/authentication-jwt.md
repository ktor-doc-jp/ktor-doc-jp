---
title: JWT Authentication
caption: Authenticating Clients with JWT / JWK  
section: Features
permalink: /features/authentication-jwt.html
---

Ktor supports client authentication using JSON Web Tokens. The currently supported implementations are JWT and JWK. Both rely on the Auth0 implementations.

### Usage

```kotlin
authentication {
  jwtAuthentication(...) { credential ->
    ...
  }
```

Install it inside the authentication feature. 

### Configuration

JWT and JWK each have their own method with slightly different parameters. 

##### JWT

The JWT authentication requires a JWTVerifier instance.

```kotlin
val realm = "ktorio.github.io"
val jwtVerifier = JWT
        .require(algorithm)
        .withAudience(audience)
        .withIssuer(issuer)
        .build()
        
jwtAuthentication(jwtVerifier, realm) { credential ->
  if (credential.payload.getClaim.contains(audience))
    JWTPrincipal(credential.payload)
  else null
}

```

##### JWK

The JWK authentication requires a JwkProvider instance. 

```kotlin
val realm = "ktorio.github.io"
val issuer = "https://jwk-provider-domain/"        
val jwkProvider = JwkProviderBuilder(issuer)
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

jwtAuthentication(jwkProvider, issuer, realm) { credential ->
  if (credential.payload.expiresAt.before(Date())) null
  else JWTPrincipal(credential.payload)
}

```

Both require the `realm` parameter, which is used in the WWW-Authenticate response header.
