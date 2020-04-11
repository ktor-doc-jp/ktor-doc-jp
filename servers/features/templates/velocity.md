---
title: Velocity
caption: Velocityテンプレートを使う
category: servers
keywords: html
feature:
  artifact: io.ktor:ktor-velocity:$ktor_version
  class: io.ktor.velocity.Velocity
redirect_from:
- /features/velocity.html
- /features/templates/velocity.html
ktor_version_review: 1.0.0
---

Ktorは[Velocity](http://velocity.apache.org/)テンプレートをVelocity Featureを通じてサポートしています。
[VelocityEngine](https://velocity.apache.org/engine/1.7/apidocs/org/apache/velocity/app/VelocityEngine.html)とともにVelocity Featureを初期化します:

{% include feature.html %}

## インストール
{: #installation}

Velocityをインストールし、`VelocityEngine`を設定します:

```kotlin
install(Velocity) {
    setProperty("resource.loader", "classpath")
    setProperty("classpath.resource.loader.class", ClasspathResourceLoader::class.java.name)
}
```

## 使い方
{: #usage}

Velocityが設定されていたら、`call.respond`を`VelocityContent`インスタンスとともに呼び出します: 

```kotlin
data class User(val name: String, val email: String)

get("/") {
	 val user = User("user name", "user@example.com")
    call.respond(VelocityContent("templates/hello.vl", user))
}
```