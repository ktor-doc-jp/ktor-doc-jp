---
title: Locations
caption: 型安全なルーティング
category: servers
permalink: /servers/features/locations.html
feature:
  artifact: io.ktor:ktor-locations:$ktor_version
  class: io.ktor.locations.Locations
redirect_from:
- /features/locations.html
ktor_version_review: 1.0.0
---

{::options toc_levels="1..2" /}

Ktor は URL の構築とリクエストパラメータの両方に対し、型安全にルーティングする機構を提供しています。

Locations は試験的な機能 (experimental feature) です。
{: .note.experimental}

**目次:**

* 目次
{:toc}

{% include feature.html %}

## Location feature のインストール
{: #installing }

特別な設定をすることなく Location feature を利用することができます。

```kotlin
install(Locations)
```

## Route クラスの定義
{: #route-classes }

型付きルーティングごとに、ルーティング内のパラメータを持つクラス (通常は data class) を作成します。

パラメータの型は [Data Conversion](/servers/features/data-conversion.html) feature でサポートされている型でなければなりません。
デフォルトでは、 `Int` 、 `Long` 、 `Float` 、 `Double` 、 `Boolean` 、 `String` 、 `Enum` 、 `Iterable` を指定できます。

### URL パラメータ
{: #parameters-url }

The names between the curly braces must match the properties of the class.

ルーティング用のクラスは `@Location` アノテーションを付与し、 `{` と `}` で囲まれたプレースホルダー (`{propertyName}` など) の名称と
同じ変数名を定義します。

```kotlin
@Location("/list/{name}/page/{page}")
data class Listing(val name: String, val page: Int)
```

* `/list/movies/page/10` にマッチする
* `Listing(name = "movies", page = 10)` が生成される

### GET パラメータ
{: #parameters-get }

`@Location` アノテーション内のプレースホルダーに無いプロパティを定義した場合、
GET クエリまたは POST パラメータで指定された値が格納されます。

```kotlin
@Location("/list/{name}")
data class Listing(val name: String, val page: Int, val count: Int)
```

* `/list/movies?page=10&count=20` にマッチする
* `Listing(name = "movies", page = 10, count = 20)` が生成される

## ルーティングハンドラの定義
{: #route-handlers }

`@Location` アノテーションを付与した [Route クラス](#route-classes) を定義すると、
Location feature はその型の `get` 、 `options` 、 `header` 、 `post` 、 `put` 、 `delete` 、 `patch` ルーティングハンドラを
生成します。


```kotlin
routing {
    get<Listing> { listing ->
        call.respondText("Listing ${listing.name}, page ${listing.page}")
    }
}
```

`io.ktor.locations` にて型パラメータを1つ持つジェネリックなメソッドが複数定義されています。
`io.ktor.routing` にも定義されている関数とほぼ同一のインタフェースで実装されています。
`locations` パッケージの前に `routing` パッケージをインポートすると、これらのパッケージをインポートする代わりに、
これらのメソッドを一般化するよう IDE が提案することがあります。
`import io.ktor.locations.*` を手動で追加するとこれが発生します。
`Locations` API はまだ試験的な機能です。
この問題はすでに [GitHub で報告](https://github.com/ktorio/ktor/issues/368) されています。
{: .note}

## URL の構築
{: #building-urls }

`@Location` アノテーションが付与されたクラスのインスタンスを `application.locations.href` の引数に渡すことで、
そのルーティングへのURLが構築されます。

```kotlin
val path = application.locations.href(Listing(name = "movies", page = 10, count = 20))
```

この例では、変数 `path` は文字列 `"/list/movies?page=10&count=20"` になります。

```kotlin
@Location("/list/{name}") data class Listing(val name: String, val page: Int, val count: Int)
```

この方式で URL を生成するようにすることで、もし URL を変更しようとした際は `@Location` パスを更新するだけで済むので非常に便利です。

## パラメータを伴うネストしたルーティング
{: #subroutes }

パラメータを伴うルーティングをネストさせる場合、 `@Location` が付与された上位のクラス (`Type`) をプロパティ (`val type: Type`) に持つ
`@Location` が付与された内部クラス (`Edit` や `List`) を作成し、下記のようにルーティングに登録します。

```kotlin
routing {
    get<Type.Edit> { typeEdit -> // /type/{name}/edit
        // ...
    }
    get<Type.List> { typeList -> // /type/{name}/list/{page}
        // ...
    }
}
```
 
```kotlin
@Location("/type/{name}") data class Type(val name: String) {
    @Location("/edit") data class Edit(val type: Type)
    @Location("/list/{page}") data class List(val type: Type, val page: Int)
}
```