---
title: Authentication
caption: Authenticating Clients  
category: features
permalink: /features/authentication.html
subsection_tag: Authentication
keywords: oauth authentication basic authentication form authentication digest authentication ldap authentication jwt authentication 
feature:
    artifact: io.ktor:ktor-auth:$ktor_version
    class: io.ktor.auth.Authentication
---

{::options toc_levels="1..2" /}

Ktor supports authentication out of the box as a standard pluggable feature.
It supports mechanisms to read *credentials*, and to authenticate *principals*.

It can be used in some cases along with the [sessions feature](/features/sessions.html)
to keep the login information between requests.

**Table of contents:**

* TOC
{:toc}

{% include feature.html %}

## Basic usage

Ktor defines two concepts: credentials and principals.

* A principal is something that can be authenticated: a user, a computer, a group, etc.
* A credential is an object that represents a set of properties for the server to authenticate a principal:
  a user/password, an API key or an authenticated payload signature, etc.

To install it, you have to call to `application.install(Authentication)`. You have to install this feature
directly to the application and it *won't* work in another `ApplicationCallPipeline` like `Route`.

You might still be able to call the install code inside a Route if you have the Application injected in a nested DSL,
but it will be applied to the application itself.
{: .note}

Using its DSL, it allows you to configure the authentication providers available:

```kotlin
install(Authentication) {
    basic(name = "myauth1") {
        realm = "Ktor Server"
        validate { credentials ->
            if (credentials.name == credentials.password) {
                UserIdPrincipal(credentials.name)
            } else {
                null
            }
        }
    }
}
```

After defining one or more authentication providers (named or unnamed), with the [routing feature](/features/routing.html)
you can create a route group, that will apply that authentication to all the routes defined in that group:

```kotlin
routing {
    authenticate("myauth1") {
        get("/authenticated/route1") {
            // ...
        }    
        get("/other/route2") {
            // ...
        }    
    }
    get("/") {
        // ...
    }
}
```

You can specify several names to apply several authentication providers, or none or null to use the unnamed one.

You can get the generated `Principal` instance inside your handler with:

```kotlin
val principal: UserIdPrincipal? = call.authentication.principal<UserIdPrincipal>()
```

In the generic, you have to put a specific type that *must* match the generated Principal.
It will return null in the case you provide another type. 
{: .note}

The handler won't be executed if the configured authentication fails (when returning `null` in the authentication mechanism)
{: .note}

## Naming the Authentication provider

It is possible to give arbitrary names to the authentication providers you specify,
or to not provide a name at all (unnamed provider) by not setting the name argument or passing a null.
 
You cannot repeat authentication provider names, and you can define just one provider without a name.

In the case you repeat a name for the provider or try to define two unnamed providers, an exception will be thrown:

```
java.lang.IllegalArgumentException: Provider with the name `authName` is already registered
```

Summarizing:

```
install(Authentication) {
    basic { // Unamed `basic` provider
        // ...
    }
    form { // Unamed `form` provider (exception, already defined a provider with name = null) 
        // ...
    }
    basic("name1") { // "name1" provider
        // ...
    }
    basic("name1") { // "name1" provider (exception, already defined a provider with name = "name1")
        // ...
    }
}
```

## Basic and Form Authentication Providers

Ktor supports two methods of authentication with the user and raw password as credentials:
`basic` and `form`.

```kotlin
install(Authentication) {
    basic(name = "myauth1") {
        realm = "Ktor Server"
        validate { credentials -> /*...*/ }
    }

    form(name = "myauth2") {
        realm = "Ktor Server"
        userParamName = "user"
        passwordParamName = "password"
        challenge = FormAuthChallenge.Unauthorized
        validate { credentials -> /*...*/ }
    }
}
```

Both authentication providers have a method `validate` to provide a callback that must generate a Principal from given a `UserPasswordCredential`
or null for invalid credentials. That callback is marked as *suspending*, so that you can validate credentials in an asynchronous fashion.

You can use several strategies for validating:

### Strategy: Manual credential validation

Since there is a validate callback for authentication, you can just put your code there.
So you can do things like checking the password against a constant, authenticating using a database
or composing several validation mechanisms.

```kotlin
application.install(Authentication) {
    basic("authName") {
        realm = "ktor"
        validate { credentials ->
            if (credentials.password == "${credentials.name}123") UserIdPrincipal(credentials.name) else null
        }
    }
}
```

Remember that both the `name` and the `password` from the credentials are arbitrary values.
Remember to escape and/or validate them when accessing with those values to the file system, a database,
when storing them, or generating HTML with its content, etc.
{: .security.note }

### Strategy: Validating using UserHashedTableAuth

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
application.install(Authentication) {
    basic("authName") {
        realm = "ktor"
        authenticate { credentials -> userTable.authenticate(credentials) }
    }
}
```

The idea here is that you are not storing the actual password but a hash, so even if your data source is leaked,
the passwords are not directly compromised. Though keep in mind that when using poor passwords and weak hashing algorithms
it is possible to do brute-force attacks. You can append (instead of prepend) long salt values and do multiple hash
stages or do key derivate functions to increase security and make brute-force attacks non-viable.
You can also enforce or encourage strong passwords when creating users.
{: .security.note}
 
### Strategy: LDAP Validation
{:#ldap}

Ktor supports LDAP (Lightweight Directory Access Protocol)
for credential authentication.

```kotlin
authentication {
    basic("authName") {
        realm = "realm"
        validate { credential ->
            ldapAuthenticate(credential, "ldap://$localhost:${ldapServer.port}", "uid=%s,ou=system")
        }
    }
}
```

Optionally you can define an additional validation check:
```kotlin
authentication {
    basic("authName") { 
        realm = "realm"
        validate { credential ->
            ldapAuthenticate(credentials, "ldap://localhost:389", "cn=%s ou=users") {
                if (it.name == it.password) {
                    UserIdPrincipal(it.name)
                } else {
                    null
                }
            }
        }
    }
}
```

You can see [advanced examples for LDAP authentication](https://github.com/ktorio/ktor/blob/master/ktor-features/ktor-auth-ldap/test/io/ktor/tests/auth/ldap/LdapAuthTest.kt) in the Ktor's tests.

In order to use this feature, you have to add the `io.ktor:ktor-auth-ldap:$ktor_version` dependency to your buildscript.
{: .artifact.note }

Bear in mind that current LDAP implementation is synchronous.
{: .performance.note}

## Digest Authentication

Ktor supports [HTTP digest authentication](https://en.wikipedia.org/wiki/Digest_access_authentication).
It works differently than the basic/form auths:

```kotlin
authentication {
    digest {
        val p = "Circle Of Life"
        digester = MessageDigest.getInstance("MD5")
        realm = "testrealm@host.com"
        userNameRealmPasswordDigestProvider = { userName, realm ->
            when (userName) {
                "missing" -> null
                else -> digest(digester, "$userName:$realm:$p")
            }
        }
    }
}
```

Instead of providing a verifier, you have to provide a `userNameRealmPasswordDigestProvider` that is in charge of
returning the `HA1` part of the digest. In the case of `MD5`: `MD5("$username:$realm:$password")`.
The idea is that [you can store passwords already hashed](https://tools.ietf.org/html/rfc2069#section-3.5).
And only return the expected hash for a specific user, or *null* if the user does not exist.
The callback is suspendable, so you can retrieve or compute the expected hash asynchronously,
for example from disk or a database.

```kotlin
authentication {
    val myRealm = "MyRealm"
    val usersInMyRealmToHA1: Map<String, ByteArray> = mapOf(
        // pass="test", HA1=MD5("test:MyRealm:pass")="fb12475e62dedc5c2744d98eb73b8877"
        "test" to hex("fb12475e62dedc5c2744d98eb73b8877")
    )

    digest("auth") {
        userNameRealmPasswordDigestProvider = { userName, realm ->
            usersInMyRealmToHA1[userName]
        }
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
{:#jwt}

Ktor supports [JWT (JSON Web Tokens)](https://jwt.io/), which is a mechanism for authenticating JSON-encoded payloads.
It is useful to create stateless authenticated APIs in the standard way, since there are client libraries for it
in a myriad of languages.

This feature will handle `Authorization: Bearer <JWT-TOKEN>`.

In order to use this authentication method, you need to include the `io.ktor:ktor-auth-jwt:$ktor_version` artifact.
{: .artifact.note}

Ktor has a couple of classes to use the JWT Payload as `Credential` or as `Principal`.

```kotlin
class JWTCredential(val payload: Payload) : Credential
class JWTPrincipal(val payload: Payload) : Principal
```

*Configuring server/routes:*

JWT and JWK each have their own method with slightly different parameters. 
Both require the `realm` parameter, which is used in the WWW-Authenticate response header.

Using a verifier and a validator:

The verifier will use the secret to verify the signature to trust the source.
You can also check the payload within `validate` callback to ensure everything is right and to produce a Principal.

application.conf:

```kotlin
jwt {
    domain = "https://jwt-provider-domain/"
    audience = "jwt-audience"
    realm = "ktor sample app"
}
```

JWT auth:

```kotlin
val jwtIssuer = environment.config.property("jwt.domain").getString()
val jwtAudience = environment.config.property("jwt.audience").getString()
val jwtRealm = environment.config.property("jwt.realm").getString()

install(Authentication) {
    jwt {
        realm = jwtRealm
        verifier(makeJwtVerifier(jwtIssuer), jwtIssuer)
        validate { credential ->
            if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
        }
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
install(Authentication) {
    jwt {
        verifier(jwkProvider, jwkIssuer)
        realm = jwkRealm
        validate { credentials ->
            if (credentials.payload.audience.contains(audience)) JWTPrincipal(credentials.payload) else null
        }
    }
}
```

## OAuth

OAuth defines a mechanism for authentication using external providers like Google or Facebook safely.
You can read more about [OAuth](https://oauth.net/).
Ktor has a feature to work with OAuth 1a and 2.0

A simplified OAuth 2.0 workflow:
* The client is redirected to an authorize URL for the specified provider (Google, Facebook, Twitter, Github...).
  specifying the `clientId` and a valid redirection URL.
* Once the login is correct, the provider generates an auth token using a `clientSecret` associated with that `clientId`.
* Then the client is redirected to a valid, previously agreed upon, application URL with an auth token that is signed with the `clientSecret`.
* Ktor's OAuth feature verifies the token and generates a Principal `OAuthAccessTokenResponse`.
* With the auth token, you can request, for example, the user's email or id depending on the provider.

*Example*:

```kotlin
@Location("/login/{type?}") class login(val type: String = "")

val loginProviders = listOf(
    OAuthServerSettings.OAuth2ServerSettings(
            name = "github",
            authorizeUrl = "https://github.com/login/oauth/authorize",
            accessTokenUrl = "https://github.com/login/oauth/access_token",
            clientId = "***",
            clientSecret = "***"
    )
)

install(Authentication) {
    oauth("oauth1") {
        client = HttpClient(Apache)
        providerLookup = { loginProviders[it.type] }
        urlProvider = { url(login(it.name)) }
    }
}

routing {
    authenticate("oauth1") {
        location<login>() {
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
    }
}
```
{: .compact}

Depending on the OAuth version, you will get a different Principal

```kotlin
sealed class OAuthAccessTokenResponse : Principal {
    data class OAuth1a(
        val token: String, val tokenSecret: String,
        val extraParameters: Parameters = Parameters.Empty
    ) : OAuthAccessTokenResponse()

    data class OAuth2(
        val accessToken: String, val tokenType: String,
        val expiresIn: Long, val refreshToken: String?,
        val extraParameters: Parameters = Parameters.Empty
    ) : OAuthAccessTokenResponse()
}
```

## Advanced

If you want to create custom authentication strategies,
you can check the [Authentication feature](https://github.com/ktorio/ktor/tree/master/ktor-features/ktor-auth/src/io/ktor/auth) as a reference.

The authentication feature defines two stages as part of its [Pipeline](https://github.com/ktorio/ktor/blob/master/ktor-features/ktor-auth/src/io/ktor/auth/AuthenticationPipeline.kt): `RequestAuthentication` and `CheckAuthentication`.
{: .note}
