---
title: 認証
caption: クライアントの認証
category: servers
permalink: /servers/features/authentication.html
children: /servers/features/authentication/
keywords: authentication
feature:
  artifact: io.ktor:ktor-auth:$ktor_version
  class: io.ktor.auth.Authentication
redirect_from:
- /features/authentication.html
ktor_version_review: 1.0.0
---

{::options toc_levels="1..2" /}

Ktorは標準のプラグイン可能なFeatureとして、すぐに使える認証機能をサポートしています。
*credential*の読み込みと*principal*の認証のメカニズムをサポートしています。

リクエスト間でログイン情報を保持するために[session機能](/servers/features/sessions.html)とともに使われるケースもあります。

**目次:**

* TOC
{:toc}

{% include feature.html %}

## メカニズム

{% include list-children.html %}

## 基本的な使い方

Ktorはcredentialとprincipalという2つの概念を定義しています。

* principalは認証されている何かを指します。ユーザやコンピュータやグループなどが例です。
* credentialは、principalを認証するサーバーに提示するプロパティのセットです。user/password、APIキー、authenticated payload signatureなどが例です。

インスタンスするたえには、`application.install(Authentication)`を呼び出す必要があります。
applicationに対し直接インストールする必要があり、`Route`のような別の`ApplicationCallPipeline`内ではうごきません。

Routeの内側でインストールするコードを実行することができますが、適用されるのはapplicationそのものに対してです。
{: .note}

DSLを使うことで、認証プロバイダの設定を可能にします:

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

1つ以上の認証プロバイダを（名前付きでも名前無しでも）定義した後、[routing機能](/servers/features/routing.html)を使って、
定義した認証が適用されるようなrouteのグループを作成できます。

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

いくつかの認証プロバイダを適用するために名前を指定することもできますし、あるいは名前を指定せず使うこともできます。

生成された`Principal`のインスタンスをhandlerの内側で以下のように取得することができます:

```kotlin
val principal: UserIdPrincipal? = call.authentication.principal<UserIdPrincipal>()
```

一般的には、生成したPrincipalにマッチする*必要がある*特定の型を指定する必要があります。
別の型を指定した場合、nullが返ります。
{: .note}

設定された認証が失敗した場合handlerは実行されません（認証メカニズム内で`null`が返ったとき）
{: .note}

## AuthenticationProviderに名前付け

指定の認証プロバイダに任意の名前をつけることができますし、あるいは名前の引数を指定しないまたはnullを渡すことで名前をつけないこともできます。

認証プロバイダ名を重複させることはできず、また名前なしのプロバイダは１つだけしか定義できません。

プロバイダの名前を重複させた場合や、2つの未命名のプロバイダがあった場合は、exceptionが投げられます:

```
java.lang.IllegalArgumentException: Provider with the name `authName` is already registered
```

要約すると以下のようになります:

```kotlin
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

## AuthenticationProviderのスキップ

基準にしたがって認証をスキップさせることもできます。

```kotlin
/**
 * Authentication filters specifying if authentication is required for particular [ApplicationCall]
 * If there is no filters, authentication is required. If any filter returns true, authentication is not required.
 */
fun AuthenticationProvider.skipWhen(predicate: (ApplicationCall) -> Boolean)
```

例えば、すでにセッションがある場合にbasic認証をスキップさせるためには、以下のように書けます:

```kotlin
authentication {
    basic {
        skipWhen { call -> call.sessions.get<UserSession>() != null }
    }
}
```

## 発展的内容

カスタムの認証を行いたい場合は、[Authentication機能](https://github.com/ktorio/ktor/tree/master/ktor-features/ktor-auth/jvm/src/io/ktor/auth)のページを参照してください。

認証機能は[Pipeline](https://github.com/ktorio/ktor/blob/master/ktor-features/ktor-auth/jvm/src/io/ktor/auth/AuthenticationPipeline.kt)において`RequestAuthentication`と`CheckAuthentication`という2つのステージで定義されています
{: .note}
