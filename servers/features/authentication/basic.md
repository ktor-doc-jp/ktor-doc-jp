---
title: Basic・Form
caption: Basic認証とForm認証
category: servers
redirect_from:
- /features/authentication/basic.html
ktor_version_review: 1.0.0
---

Ktorはユーザと平文パスワードによる認証メソッドを2つサポートしています:
`basic`と`form`です。

```kotlin
install(Authentication) {
    basic(name = "myauth1") {
        realm = "Ktor Server"
        validate { credentials -> /*...*/ }
    }

    form(name = "myauth2") {
        userParamName = "user"
        passwordParamName = "password"
        challenge = FormAuthChallenge.Unauthorized
        validate { credentials -> /*...*/ }
    }
}
```

どちらの認証プロバイダも`validate`メソッドを持っており、このメソッドに
`UserPasswordCredential`からPrincipalを生成するか、不正なクレデンシャルからnullを生成するcallbackを渡す必要があります。
callbackは*suspending*としてマークされているため、クレデンシャルのバリデーションを非同期に行うこともできます。 

バリデーションにはいくつかの戦略が使えます:

## 戦略: 手動のクレデンシャルバリデーション

認証に対するバリデーションを行うcallbackがあるので、コードをそこに書くことができます。
そのため、パスワードが定数と一致するかや、データベースを使った認証や、いくつかのバリデーションメカニズムを組み合わせた認証などを行うことができます。

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

クレデンシャルにある`name`と`password`はともに任意の値だということを覚えておいてください。
ファイルシステムやデータベースにアクセスするときや、保存するとき、HTMLを生成するときなどに、それらの値をエスケープした上でバリデーションを行う必要があることを忘れないでください。
{: .security.note }

## 戦略: UserHashedTableAuthを用いたバリデーション

`UserPasswordCredential`を認証するためのインメモリのパスワードハッシュを扱うクラスがあります。
コード内の定数からか、あるいは別のコードから、利用することができます。
事前に定義されたdigest関数を使うことも、あなた自身で書いたものを使うこともできます。

*インスタンス化:*

```kotlin
val userTable = UserHashedTableAuth(getDigestFunction("SHA-256", salt = "ktor"), mapOf(
    "test" to decodeBase64("VltM4nfheqcJSyH887H+4NEOm2tDuKCl83p5axYXlF0=") // sha256 for "test"
))
```

*server/routeの設定:*

```kotlin
application.install(Authentication) {
    basic("authName") {
        realm = "ktor"
        authenticate { credentials -> userTable.authenticate(credentials) }
    }
}
```

ここでのアイデアは、実際のパスワードではなくハッシュを保存し、データがリークした場合でもパスワードは直接侵害されないようにするというものです。
しかし、弱いパスワードを使ったり、弱いハッシュ化アルゴリズムを使うと、brute-forceアタックができてしまうことを忘れないでください。
長いsalt値を付与し、複数回のハッシュ化を行うかセキュリティ向上できるような関数を実行するかすることでbrute-forceアタックを実行不可能にできます。
また、ユーザに強力なパスワードを設定するよう強制または奨励することもできます。
{: .security.note}
