---
title: Freemarker
caption: Freemarkerテンプレートを使う
category: servers
keywords: html
feature:
  artifact: io.ktor:ktor-freemarker:$ktor_version
  class: io.ktor.freemarker.FreeMarker
redirect_from:
- /features/freemarker.html
- /features/templates/freemarker.html
ktor_version_review: 1.0.0
---

Ktorは[FreeMarker](http://freemarker.org/)テンプレートをFreeMarker Featureを通じてサポートしています。
[TemplateLoader](http://freemarker.org/docs/pgui_config_templateloading.html)とともにFreeMarker Featureを初期化します:

```kotlin
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }
```

このTemplateLoaderは、FreeMarkerがテンプレートファイルをクラスパス上で現在のクラスパスからの相対パスとして"templates"パッケージ内で見つけられるようにセットアップします。
基本的なテンプレートは以下のような見た目になります:

{% include feature.html %}

```html
<html>
<h2>Hello ${user.name}!</h2>

Your email address is ${user.email}
</html>
```

アプリケーション内のどこででも`call.respond()`メソッドを利用することで、この`resources/templates`のテンプレートにアクセスできます。

```kotlin
data class User(val name: String, val email: String)

get("/") {
	val user = User("user name", "user@example.com")
	call.respond(FreeMarkerContent("hello.ftl", mapOf("user" to user)))
}
```
