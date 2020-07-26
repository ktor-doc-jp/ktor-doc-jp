---
title: 静的コンテンツ
caption: 静的コンテンツの配信
category: servers
permalink: /servers/features/static-content.html
feature:
  artifact: io.ktor:ktor-server-core:$ktor_version
  class: io.ktor.routing.Routing
redirect_from:
- /features/static-content.html
ktor_version_review: 1.0.0
---

Ktorはビルトインで静的コンテンツ配信をサポートしています。
スタイルシートやスクリプトや画像等を配信したいときに便利な機能です:

{% include feature.html %}

## ファイルとフォルダの指定

`static`関数を使い、Ktorに特定URIを静的コンテンツのように扱うよう指定し、そのファイルがどこにあるのかも定義することができます。
すべてのコンテンツは現在のworkingディレクトリからの相対パスで扱われます。
異なるルートフォルダを設定するには[カスタムルートフォルダの定義](#defining-a-custom-root-folder)をご覧ください。
      
```kotlin
routing {
    static("static") {
        files("css") 
    }
}
```

上の例は`/static`のURIに来た任意のリクエストを静的コンテンツのように扱うよう`ktor`に伝えます。
`files("css")`はそれらのファイルが配置されているフォルダを定義します。
つまり`css`フォルダ配下の任意のファイルが配信されます。
つまり、**`/static/styles.css`**といったリクエストは**`css/styles.css`**ファイルを配信します。

フォルダに加え、`file`を使うことで指定したファイルを含めることもできます。
実際の物理的なファイル名とURIのパス名が違った場合には、オプショナルの第二引数に実際の物理的なファイル名を指定することもできます。

```kotlin
routing {
    static("static") {
        files("css")
        files("js")
        file("image.png")
        file("random.txt", "image.png")
        default("index.html")
    }
}
```

`default`を使うことで、配信するデフォルトのファイルを指定することもできます。
例えば

**`/static`** をファイル名無しで呼び出した場合,  **`index.html`**が配信されるようにすることができます。

## カスタムのルートフォルダの定義

workingディレクトリ以外の異なるルートフォルダを指定するためには、
`staticRootFolder`に`File`型で値を指定します。

```kotlin
static("custom") {
    staticRootFolder = File("/system/folder/docs")
    files("public")
}
```

## サブルートの定義

サブルートの定義もできます。
例えば`/static/themes`などです。

```kotlin
static("static") {
    files("css")
    static("themes") {
        files("data")
    }
}
```

## 組み込みリソースの配信

静的コンテンツをリソースとしてアプリケーションに組み込む場合は、
`resource`、`resources`関数を使ってリソースから配信することができます:

```kotlin
static("static") {
    resources("css")
    resource("favicon.ico")
}
```

`default`に似た`defaultResource`というものもあり、フォルダに対し配信するデフォルトページの指定することができます。
`staticRootFolder`に似た`staticBasePackage`というものもあり、静的コンテンツのベースリソースパッケージを指定することができます。

## エラーハンドリング

リクエストされたコンテンツが見つからない場合、`404 Not Found`が返されます。

## コンテントタイプのカスタマイズ

ファイルが配信されるとき、コンテントタイプは`ContentType.defaultForFile(file)`を使ってファイル拡張子から決定されます。
各ファイルタイプに対応するデータは`ktor-server-core`に配置された`mimelist.csv`リソースファイルから取得されます。

## 内部実装

`static`関数は以下のように定義されています。

```kotlin
fun Route.static(remotePath: String, configure: Route.() -> Unit) = route(remotePath, configure)
````

これは本質的にはただすこし違った形でルート定義しているだけです。

## 静的コンテンツに対するHEADリクエストを扱う
{: #head-requests }

Ktorは`HEAD`リクエストをデフォルトでは扱うことができません。
それゆえに静的コンテンツ機能も`HEAD`リクエストを扱うことができません。
自動的に`HEAD`リクエストを各`GET`ルートごとに扱えるようにするには、
[`AutoHeadResponse` Feature](/servers/features/autoheadresponse.html)をインストールしてください。

```kotlin
fun Application.main() {
    // ...
    install(AutoHeadResponse) 
    // ...
}
```

デフォルトではHEADリクエストをハンドリングできないということは、
もしAutoHeadResponse Featureをインストールせず`curl -I`か`curl --head`をGETルートに対して使った場合、
[404 Not Foundが返ることになります](/quickstart/faq.html#curl-head-not-found)。

