---
title: Mustache
caption: Mustacheテンプレートを使う
category: servers
keywords: html
feature:
  artifact: io.ktor:ktor-mustache:$ktor_version
  class: io.ktor.mustache.Mustache
redirect_from:
- /features/mustache.html
- /features/templates/mustache.html
ktor_version_review: 1.1.1
---

KtorはMustache Featureを通じて、[Mustache](https://github.com/spullara/mustache.java)テンプレートをサポートしています。
[MustacheFactory](http://spullara.github.io/mustache/apidocs/com/github/mustachejava/MustacheFactory.html)を使って、
Mustache Featureを初期化できます。

```kotlin
    install(Mustache) {
        mustacheFactory = DefaultMustacheFactory("templates")
    }
```

このMustacheFactoryは、Mustacheがテンプレートファイルをクラスパス上で現在のクラスパスからの相対パスとして"templates"パッケージ内で見つけられるようにセットアップします。
基本的なテンプレートは以下のような見た目になります:

{% include feature.html %}

{% raw %}
```html
<html>
<h1>Hello {{ user.name }}</h1>

Your email address is {{ user.email }}
</html>
```
{% endraw %}

アプリケーション内のどこででも`call.respond()`メソッドを利用することで、この`resources/templates`のテンプレートにアクセスできます。

```kotlin
data class User(val name: String, val email: String)
get("/") {
    val user = User("user name", "user@example.com")
    call.respond(MustacheContent("hello.hbs", mapOf("user" to user)))
}
```