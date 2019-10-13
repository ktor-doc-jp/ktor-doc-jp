---
title: Cookie
category: clients
caption: Cookie
feature:
  artifact: io.ktor:ktor-client-core:$ktor_version
  class: io.ktor.client.features.Cookies
  method: io.ktor.client.features.cookies
ktor_version_review: 1.2.0
---

この機能を使うことで呼び出し間でCookieを保持するか特定のCookieを強制することができます。

{% include feature.html %}

## インストール

```kotlin
val client = HttpClient() {
    install(HttpCookies) {
        // Will keep an in-memory map with all the cookies from previous requests.
        storage = AcceptAllCookiesStorage()

        // Will ignore Set-Cookie and will send the specified cookies.
        storage = ConstantCookieStorage(Cookie("mycookie1", "value"), Cookie("mycookie2", "value"))
    }
}

client.cookies("mydomain.com")
```
