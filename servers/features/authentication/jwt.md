---
title: JWT・JWK
caption: JWT認証・JWK認証
category: servers
keywords: jwt authentication jwt
feature:
  artifact: io.ktor:ktor-auth-jwt:$ktor_version
  method: io.ktor.auth.jwt.jwt
redirect_from:
- /features/authentication/jwt.html
ktor_version_review: 1.0.0
---

Ktorは[JWT (JSON Web Tokens)](https://jwt.io/)をサポートしています。
JWTはエンコードされたJSONペイロードによって認証する仕組みです。
ステートレスな認証が必要なAPIをスタンダードな方法で作る上で役に立ち、たくさんの言語によってJWTをサポートするクライアントライブラリもあります。

この機能は`Authorization: Bearer <JWT-TOKEN>`を扱います。

{% include feature.html %}

Ktorは`Credential`または`Principal`としてJWTペイロードを利用するクラスをいくつか持ちます。

```kotlin
class JWTCredential(val payload: Payload) : Credential
class JWTPrincipal(val payload: Payload) : Principal
```

## server/routeの設定:

JWTとJWKはそれぞれ少しずつ違ったパラメータのメソッドを持ちます。
ともに`realm`パラメータを必要とし、それはWWW-Authenticateレスポンスヘッダーで使われます。

## verifierとvalidatorの使用:

verifierはsecretを署名を検証しソースを信頼するために利用します。
諸々正しいことを検証しPrincipalを生成するために、`validate`コールバック内でpayloadのチェックも行うことができます。

### application.conf:

```kotlin
jwt {
    domain = "https://jwt-provider-domain/"
    audience = "jwt-audience"
    realm = "ktor sample app"
}
```

### JWT auth:

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

## JWKプロバイダの利用:

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
