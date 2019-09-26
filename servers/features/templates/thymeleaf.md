---
title: Thymeleaf
caption: Thymeleafテンプレートを使う
category: servers
keywords: html
feature:
  artifact: io.ktor:ktor-thymeleaf:$ktor_version
  class: io.ktor.thymeleaf.Thymeleaf
redirect_from:
- /features/thymeleaf.html
- /features/templates/thymeleaf.html
ktor_version_review: 1.2.0
---

Ktorは[Thymeleaf](https://www.thymeleaf.org/)テンプレートをThymeleaf Featureを通じてサポートしています。
[ClassLoaderTemplateResolver](https://www.thymeleaf.org/apidocs/thymeleaf/3.0.1.RELEASE/org/thymeleaf/templateresolver/ClassLoaderTemplateResolver.html)とともにThymeleaf Featureを初期化します:

```kotlin
    install(Thymeleaf) {
        setTemplateResolver(ClassLoaderTemplateResolver().apply { 
            prefix = "templates/"
            suffix = ".html"
            characterEncoding = "utf-8"
        })
    }
```

このTemplateLoaderは、Thymeleafがテンプレートファイルをクラスパス上で現在のクラスパスからの相対パスとして"templates"パッケージ内で見つけられるようにセットアップします。
基本的なテンプレートは以下のような見た目になります:

{% include feature.html %}

```html
<!DOCTYPE html >
<html xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8">
  <title>Title</title>
</head>
<body>
<span th:text="${user.name}"></span>
</body>
</html>
```

アプリケーション内のどこででも`call.respond()`メソッドを利用することで、この`resources/templates`のテンプレートにアクセスできます。

```kotlin
    get("/") {
        call.respond(ThymeleafContent("index", mapOf("user" to User(1, "user1"))))
    }
```
