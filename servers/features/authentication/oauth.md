---
title: OAuth
caption: OAuth認証
category: servers
redirect_from:
- /features/authentication/oauth.html
ktor_version_review: 1.0.0
---

OAuthはGoogleやFacebookなどの外部プロバイダを使った安全な認証メカニズムを定義しています。
[OAuth](https://oauth.net/)を読めばより詳細について知れます。
KtorにはOAuth 1aと2.0で動作するFeatureがあります。

シンプルなOAuth 2.0によるワークフロー:
* クライアントは、`clientId`と有効なリダイレクト先URLを指定した上で、特定プロバイダ（Google, Facebook, Twitter, Github）の認証URLへリダイレクトされます。
* ログインに成功したら、プロバイダは`clientId`に紐づく`clientSecret`を使って認証トークンを生成します。
* 次に、クライアントは以前合意した有効なアプリケーションURLへと、`clientSecret`で署名された認証トークン付きでリダイレクトされます。
* KtorのOAuth機能はトークンの検証と、Principal `OAuthAccessTokenResponse`の生成を行います。
* 認証トークンを使って、例えばユーザのemailやprovider内のidをリクエストすることができます。

*例*:

{% capture oauth-sample-kt %}
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
).associateBy {it.name}

install(Authentication) {
    oauth("gitHubOAuth") {
        client = HttpClient(Apache)
        providerLookup = { loginProviders[application.locations.resolve<login>(login::class, this).type] }
        urlProvider = { url(login(it.name)) }
    }
}

routing {
    authenticate("gitHubOAuth") {
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
{% endcapture %}

{% include tabbed-code.html
    tab1-title="AuthSample.kt" tab1-content=oauth-sample-kt
    no-height="true"
%}

OAuthバージョンによって、異なるPrincipalが取得されます。

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

## ガイド、サンプル、テスト

* [OAuthガイド](/quickstart/guides/oauth.html)
* [いくつかのOAuthプロバイダの設定方法例](https://github.com/ktorio/ktor-samples/blob/master/feature/auth/src/io/ktor/samples/auth/OAuthLoginApplication.kt)
* [OAuth認証のテスト](https://github.com/ktorio/ktor-samples/commit/56119d2879d9300cf51d66ea7114ff815f7db752)
