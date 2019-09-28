---
title: LDAP
caption: LDAP認証
category: servers
feature:
  artifact: io.ktor:ktor-auth-ldap:$ktor_version
  package: io.ktor.auth.ldap
redirect_from:
- /features/authentication/ldap.html
ktor_version_review: 1.0.0
---

KtorはLDAP (Lightweight Directory Access Protocol)認証をサポートしています。

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

オプションとして、追加のバリデーションチェックを定義できます:
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

シグニチャは以下のようになります:

```kotlin
// Simplified signatures
fun ldapAuthenticate(credential: UserPasswordCredential, ldapServerURL: String, userDNFormat: String): UserIdPrincipal?
fun ldapAuthenticate(credential: UserPasswordCredential, ldapServerURL: String, userDNFormat: String, validate: InitialDirContext.(UserPasswordCredential) -> UserIdPrincipal?): UserIdPrincipal?
```

To support more complex scenarios, there is a more complete signature for `ldapAuthenticate`:

```kotlin
fun <K : Credential, P : Any> ldapAuthenticate(credential: K, ldapServerURL: String, ldapEnvironmentBuilder: (MutableMap<String, Any?>) -> Unit = {}, doVerify: InitialDirContext.(K) -> P?): P?
```

While the other overloads support only `UserPasswordCredential`, this overload accept any kind of credential. And instead of receiving a string with the userDNFormat, you can provide a generator
to populate a map with the environments for ldap.

以下はこれを使ったより発展的な例です:

```kotlin
application.install(Authentication) {
    basic {
        validate { credential ->
            ldapAuthenticate(
                credential,
                "ldap://$localhost:${ldapServer.port}",
                configure = { env: MutableMap<String, Any?> -> 
                    env.put("java.naming.security.principal", "uid=admin,ou=system")
                    env.put("java.naming.security.credentials", "secret")
                    env.put("java.naming.security.authentication", "simple")
                }
            ) {
                val users = (lookup("ou=system") as LdapContext).lookup("ou=users") as LdapContext
                val controls = SearchControls().apply {
                    searchScope = SearchControls.ONELEVEL_SCOPE
                    returningAttributes = arrayOf("+", "*")
                }

                users.search("", "(uid=user-test)", controls).asSequence().firstOrNull {
                    val ldapPassword = (it.attributes.get("userPassword")?.get() as ByteArray?)?.toString(Charsets.ISO_8859_1)
                    ldapPassword == credential.password
                }?.let { UserIdPrincipal(credential.name) }
            }
        }
    }
}
```

Ktorのテストにある[LDAP認証の発展的な例](https://github.com/ktorio/ktor/blob/master/ktor-features/ktor-auth-ldap/test/io/ktor/tests/auth/ldap/LdapAuthTest.kt)をご覧いただけます。

{% include feature.html %}

LDAP実装は同期的であることに留意してください
{: .performance.note}