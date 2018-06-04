---
title: LDAP
caption: LDAP authentication
category: features
---

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
