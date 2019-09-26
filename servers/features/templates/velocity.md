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
install(Velocity) { // this: VelocityEngine
    setProperty("resource.loader", "string");
    addProperty("string.resource.loader.class", StringResourceLoader::class.java.name)
    addProperty("string.resource.loader.repository.static", "false")
    init() // need to call `init` before trying to retrieve string repository
    
    (getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT) as StringResourceRepository).apply {
        putStringResource("test.vl", "<p>Hello, \$id</p><h1>\$title</h1>")
    }
}
```

## 使い方
{: #usage}

Velocityが設定されていたら、`call.respond`を`VelocityContent`インスタンスとともに呼び出します: 

```kotlin
routing {
    val model = mapOf("id" to 1, "title" to "Hello, World!")

    get("/") {
        call.respond(VelocityContent("test.vl", model, "e"))
    }
}
```