---
title: HTML DSL
caption: DSLを使ってHTMLを配信
category: servers
feature:
  artifact: io.ktor:ktor-html-builder:$ktor_version
  class: io.ktor.html.HtmlContent
redirect_from:
- /features/html-dsl.html
- /features/templates/html-dsl.html
ktor_version_review: 1.0.0
---

この機能は[kotlinx.html](https://github.com/Kotlin/kotlinx.html)と統合されており、
HTMLをChunked transfer encodingを使って直接配信しているため、HTML全体のためにメモリ空間を保持する必要がありません。

{% include feature.html %}

## インストール
{: #install }

この機能はインストールを必要としません。

## 基本的な使い方
{: #basic-usage }

レスポンスの生成時に、`respond`/`respondText`メソッドを呼び出す代わりに、`ApplicationCall.respondHtml`を呼び出します:

```kotlin
call.respondHtml {
    head {
        title { +"Async World" }
    }
    body {
        h1(id = "title") {
            +"Title"
        }
    }
}
```

kotlinx.htmlを使ってHTMLを生成する方法に関するドキュメントとしては、[wiki](https://github.com/kotlin/kotlinx.html/wiki/Getting-started)を参照ください。

## テンプレートとレイアウト
{: #templates-and-layouts }

DSLによるプレーンなHTMLの生成に加え、Ktorでは型付けされたシンプルなテンプレートエンジンが利用できます。
それを使うことで複雑なレイアウトを型安全な方法で生成できます。
とてもシンプルですが、パワフルです:

```kotlin
call.respondHtmlTemplate(MulticolumnTemplate()) {
    column1 {
        +"Hello, $name"
    }
    column2 {
        +"col2"
    }
}

class MulticolumnTemplate(val main: MainTemplate = MainTemplate()) : Template<HTML> {
    val column1 = Placeholder<FlowContent>()
    val column2 = Placeholder<FlowContent>()
    override fun HTML.apply() {
        insert(main) {
            menu {
                item { +"One" }
                item { +"Two" }
            }
            content {
                div("column") {
                    insert(column1)
                }
                div("column") {
                    insert(column2)
                }
            }
        }
    }
}

class MainTemplate : Template<HTML> {
    val content = Placeholder<HtmlBlockTag>()
    val menu = TemplatePlaceholder<MenuTemplate>()
    override fun HTML.apply() {
        head {
            title { +"Template" }
        }
        body {
            h1 {
                insert(content)
            }
            insert(MenuTemplate(), menu)
        }
    }
}

class MenuTemplate : Template<FlowContent> {
    val item = PlaceholderList<UL, FlowContent>()
    override fun FlowContent.apply() {
        if (!item.isEmpty()) {
            ul {
                each(item) {
                    li {
                        if (it.first) b {
                            insert(it)
                        } else {
                            insert(it)
                        }
                    }
                }
            }
        }
    }
}
```

この例のように、`Template<TFlowContent>`を実装したクラスを定義し、`TFlowContent.apply`をオーバーライドし、
`Placeholder`か`TemplatePlaceholder`プロパティを任意で定義する必要があります。

`call.respondHtmlTemplate(MulticolumnTemplate()) { }`でテンプレートを生成するとき、
テンプレートをレシーバとして取得でき、
プロパティとして定義されたプレースホルダーに型安全な方法でアクセスすることができます。
