---
title: Digest
caption: Digest認証
category: servers
redirect_from:
- /features/authentication/digest.html
ktor_version_review: 1.0.0
---

Ktorは[HTTP digest認証](https://en.wikipedia.org/wiki/Digest_access_authentication)をサポートしています。
basic/form認証とは異なる動作をします:

```kotlin
authentication {
    digest {
        val password = "Circle Of Life"
        digester = MessageDigest.getInstance("MD5")
        realm = "testrealm@host.com"
        userNameRealmPasswordDigestProvider = { userName, realm ->
            when (userName) {
                "missing" -> null
                else -> {
                    digester.reset()
                    digester.update("$userName:$realm:$password".toByteArray())
                    digester.digest()
                }
            }
        }
    }
}
```

verifierを提供する代わりに、`userNameRealmPasswordDigestProvider`を提供しdigestの`HA1`部分を返す必要があります。
`MD5`の場合、`MD5("$username:$realm:$password")`です。
ここでの考え方は[すでにハッシュ化されているパスワードを保存する](https://tools.ietf.org/html/rfc2069#section-3.5)というものです。
そして期待されるハッシュを特定のユーザに返すか、ユーザが存在しない場合には*null*を返すだけです。
callbackはsuspend可能なので、期待されるハッシュ値の参照・計算を、例えばディスクやデータベースに対し非同期に行うことができます。

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
`HA1` (`H(A1)`) は[RFC 2069 (An Extension to HTTP: Digest Access Authentication)](https://tools.ietf.org/html/rfc2069)からきてます。  
```
HA1=MD5(username:realm:password) <-- You usually store this.
HA2=MD5(method:digestURI)
response=MD5(HA1:nonce:HA2) <-- The client and the server sends and checks this.
```
</div>

`realm`は`digestAuthentication`関数に渡された`realm`であることが保証されており、便宜的に渡されますが、
`userName`には任意の値を指定できます。
そのため、ファイルシステムへのアクセス、データベースへのアクセス、保存、HTMLの生成などにその値を使用する場合は、
これを考慮してエスケープまたは検証することを忘れないでください。
{: .security.note}