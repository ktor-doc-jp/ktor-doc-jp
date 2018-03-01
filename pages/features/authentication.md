---
title: Authentication
caption: Authenticating Clients  
section: Features
permalink: /features/authentication.html
subsection_tag: Authentication
---

{::options toc_levels="1..2" /}

Ktor supports authentication out of the box as a pluggable standard feature.
It support mechanisms to read *credentials*, and to authenticate *principals*.

It can be used in some cases along the [sessions feature](/features/sessions.html)
to keep the login information between requests.

**Table of contents:**

* TOC
{:toc}

## Basic usage

You can install this feature as normal
In addition to `install(Authentication)`, Ktor provides a shorter convenient method called `authentication`,
both available to any `ApplicationCallPipeline`, including `Application` and `Route` among others.
Using its DSL, it allows you to configure the authentication mechanisms available:

```kotlin
authentication {
    basicAuthentication("ktor") { credentials ->
        if (credentials.name == credentials.password) {
            UserIdPrincipal(credentials.name)
        } else {
            null
        }
    }
}
```

You can get the generated `Principal` instance inside your handler with:

```kotlin
val principal: UserIdPrincipal? = call.authentication.principal<UserIdPrincipal>()
```

You have to specify a generic type that *must* match the generated Principal.
It will return null in the case you provide another type. 
{: .note}

The handler won't be executed if the configured authentication fails, by returning null as the principal.
{: .note}

## Basic Authentication and Form Authentication

Ktor supports two methods of authentication with the user and raw password as credentials.

```kotlin
fun AuthenticationPipeline.basicAuthentication(realm: String, validate: suspend (UserPasswordCredential) -> Principal?)
fun AuthenticationPipeline.formAuthentication(
    userParamName: String = "user",
    passwordParamName: String = "password",
    challenge: FormAuthChallenge = FormAuthChallenge.Unauthorized,
    validate: suspend (UserPasswordCredential) -> Principal?
)
```

Both authentication methods have a mandatory `validate` callback and must generate a Principal from given a `UserPasswordCredential`
or null for invalid credentials. That callback is marked as *suspending*, so that you can validate credentials in an asynchronous fashion.

You can use several strategies for validating:

### Manual credential validation

Since there is a validate callback for authentication, you can just put your code there.
So you can do things like checking the password against a constant or composing several validation mechanisms.

```kotlin
authentication {
    basicAuthentication("ktor") { credentials ->
        if (credentials.password == "${credentials.name}123") UserIdPrincipal(credentials.name) else null
    }
}
```

### Validating using UserHashedTableAuth

There is a class that handles hashed passwords in-memory to authenticate `UserPasswordCredential`.
You can populate it from constants in code or from another source. You can use predefined digest functions
or your own.

*Instantiating:*

```kotlin
val userTable = UserHashedTableAuth(getDigestFunction("SHA-256", salt = "ktor"), mapOf(
    "test" to decodeBase64("VltM4nfheqcJSyH887H+4NEOm2tDuKCl83p5axYXlF0=") // sha256 for "test"
))
```

*Configuring server/routes:*

```kotlin
authentication {
    basicAuthentication("ktor") { credentials -> userTable.authenticate(credentials) }
}
```

*Security:*

The idea here is that you are not storing the actual password but a hash, so even if your data source is leaked,
the passwords are not directly compromised. Though keep in mind that when using poor passwords and weak hashing algorithms
it is possible to do brute-force attacks. You can append (instead of prepend) long salt values and do multiple hash
stages or do key derivate functions to increase security and make brute-force attacks non-viable.
You can also enforce or encourage strong passwords when creating users.
 
### LDAP Validation

Ktor supports LDAP for credential verification in a separate artifact `ktor-auth-ldap`.

*In your build script:*

```groovy
compile "io.ktor:ktor-auth-ldap:$ktor_version"
```

*Configuring:*

```kotlin
authentication {
    basicAuthentication("realm") { credential ->
        ldapAuthenticate(credential, "ldap://$localhost:${ldapServer.port}", "uid=%s,ou=system")
    }
}
```

Optionally you can define an additional validation check:
```kotlin
authentication {
    basicAuthentication("realm") { credential ->
        ldapAuthenticate(credentials, "ldap://localhost:389", "cn=%s ou=users") {
            if (it.name == it.password) {
                UserIdPrincipal(it.name)
            } else null
        }
    }
}
```

You can see [advanced examples in tests](https://github.com/ktorio/ktor/blob/master/ktor-features/ktor-auth-ldap/test/io/ktor/tests/auth/ldap/LdapAuthTest.kt).

Note: Bear in mind that current LDAP implementation is synchronous.

## Digest Authentication

Ktor supports [HTTP digest authentication](https://en.wikipedia.org/wiki/Digest_access_authentication). But the API is slightly different than the Basic and Form authentication mechanisms:

```kotlin
fun AuthenticationPipeline.digestAuthentication(
    realm: String = "ktor",
    digestAlgorithm: String = "MD5",
    digesterProvider: (String) -> MessageDigest = { MessageDigest.getInstance(it) },
    userNameRealmPasswordDigestProvider: suspend (userName: String, realm: String) -> ByteArray?
)
```

Instead of providing a verifier, you have to provide a `userNameRealmPasswordDigestProvider` that is in charge of
returning the `HA1` part of the digest. In the case of `MD5`: `MD5("$username:$realm:$password")`.
The idea is that [you can store passwords already hashed](https://tools.ietf.org/html/rfc2069#section-3.5).
And just return the expected hash for a specific user, or *null* if the user do not exist.
The callback is suspendable so you can retrieve or compute the expected hash asynchronously,
for example from disk or from a database.

```kotlin
authentication {
    val myRealm = "MyRealm"
    val usersInMyRealmToHA1: Map<String, ByteArray> = mapOf(
        // pass="test", HA1=MD5("test:MyRealm:pass")="fb12475e62dedc5c2744d98eb73b8877"
        "test" to hex("fb12475e62dedc5c2744d98eb73b8877")
    )

    digestAuthentication(realm = myRealm) { userName, realm ->
        usersInMyRealmToHA1[userName]
    }
}
```

<div markdown="1" class="note" style="margin-bottom:1em;">
`HA1` (`H(A1)`) comes from [RFC 2069 (An Extension to HTTP: Digest Access Authentication)](https://tools.ietf.org/html/rfc2069)  
```
HA1=MD5(username:realm:password) <-- You usually store this.
HA2=MD5(method:digestURI)
response=MD5(HA1:nonce:HA2) <-- The client and the server sends and checks this.
```
</div>

While `realm` is guaranteed to be the `realm` passed to the `digestAuthentication` function and it is passed just for convenience,
`userName` *can* be any value, so take this into account and remember to escape and or validate it, when using that value
for accessing the file system, accessing databases, storing it, generating HTML, etc.
{: .security.note}

## Authenticating APIs using JWT

Ktor supports [JWT (JSON Web Tokens)](https://jwt.io/), which is a mechanism for authenticating json-encoded payloads.
It is useful to create stateless authenticated API in a standard way with client libraries in a myriad of languages.

Ktor will handle `Authorization: Bearer <JWT-TOKEN>` 

Ktor has a couple of classes to use the JWT Payload as Credential or as Principal.

```kotlin
class JWTCredential(val payload: Payload) : Credential
class JWTPrincipal(val payload: Payload) : Principal
```

*In your build script:*

```groovy
compile "io.ktor:ktor-auth-jwt:$ktor_version"
```

*Configuring server/routes:*

JWT and JWK each have their own method with slightly different parameters. 
Both require the `realm` parameter, which is used in the WWW-Authenticate response header.

Using a verifier and a validator:

The verifier will use the secret to verify the signature to trust the source.
You can also check the payload within `validate` callback to ensure everything is right and to produce a Principal

```kotlin
fun AuthenticationPipeline.jwtAuthentication(jwtVerifier: JWTVerifier, realm: String, validate: (JWTCredential) -> Principal?)
```

```kotlin
val jwtSecret = "secret"
val jwtRealm = "ktor jwt auth test"
val jwtVerifier = JWT.require(Algorithm.HMAC256(jwtSecret))
    .withAudience(audience)
    .withIssuer(issuer)
    .build()

authentication {
    jwtAuthentication(jwtVerifier, jwtRealm) { credentials ->
        if (credentials.payload.audience.contains(audience)) JWTPrincipal(credentials.payload) else null
    }
}
```

Using a JWK provider:

```kotlin
fun AuthenticationPipeline.jwtAuthentication(jwkProvider: JwkProvider, issuer: String, realm: String, validate: (JWTCredential) -> Principal?)
```

```kotlin
val jwkIssuer = "https://jwt-provider-domain/"
val jwkRealm = "ktor jwt auth test"
val jwkProvider = JwkProviderBuilder(jwkIssuer)
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()
authentication {
    jwtAuthentication(jwkProvider, jwkIssuer, jwkRealm) { credentials ->
        if (credentials.payload.audience.contains(audience)) JWTPrincipal(credentials.payload) else null
    }
}
```

## OAuth

OAuth defines a mechanism for authentication using external providers like Google or Facebook safely.
You can read more about OAuth [here](https://oauth.net/)
Ktor has a feature to work with OAuth 1a and 2.0

A simplified OAuth 2.0 workflow:
* The client is redirected to an authorize URL for the specified provider (Google, Facebook, Twitter, Github...).
  specifying the `clientId` and a valid redirection url.
* Once the login is correct, the provider generates an auth token using a `clientSecret` associated with that `clientId`.
* Then the client is redirected to a valid, previously agreed upon, application URL with an auth token that is signed with the `clientSecret`.
* Ktor's OAuth feature verifies the token and generates a Principal `OAuthAccessTokenResponse`.
* With the auth token, you can request, for example, the user's email or id depending on the provider.

### Basic usage

```kotlin
val loginProviders = listOf(
    OAuthServerSettings.OAuth2ServerSettings(
            name = "github",
            authorizeUrl = "https://github.com/login/oauth/authorize",
            accessTokenUrl = "https://github.com/login/oauth/access_token",
            clientId = "***",
            clientSecret = "***"
    )
)

@Location("/login/{type?}") class login(val type: String = "")

location<login>() {
    authentication {
        oauthAtLocation<login>(client, exec.asCoroutineDispatcher(),
                providerLookup = { loginProviders[it.type] },
                urlProvider = { _, p -> redirectUrl(login(p.name), false) })
    }

    param("error") {
        handle {
            call.loginFailedPage(call.parameters.getAll("error").orEmpty())
        }
    }

    handle {
        val principal = call.authentication.principal<OAuthAccessTokenResponse>()
        if (principal != null) {
            call.loggedInSuccessResponse(principal)
        } else {
            call.loginPage()
        }
    }
}
```
{: .compact}

Depending on the OAuth version, you will get a different Principal

```kotlin
sealed class OAuthAccessTokenResponse : Principal {
    data class OAuth1a(val token: String, val tokenSecret: String, val extraParameters: Parameters = Parameters.Empty) : OAuthAccessTokenResponse()
    data class OAuth2(val accessToken: String, val tokenType: String, val expiresIn: Long, val refreshToken: String?, val extraParameters: Parameters = Parameters.Empty) : OAuthAccessTokenResponse()
}
```

## Advanced

It defines two stages as part of its Pipeline: `RequestAuthentication` and `CheckAuthentication`.
