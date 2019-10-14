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

Some of these generic methods with one type parameter, defined in the `io.ktor.locations`,
have the same name as other methods defined in the `io.ktor.routing` package.
If you import the routing package before the locations one,
the IDE might suggest you generalize those methods instead of importing the right package.
You can manually add `import io.ktor.locations.*` if that happens to you.
Remember this API is experimental. This issue is already [reported at github](https://github.com/ktorio/ktor/issues/368).
{: .note}


## Building URLs
{: #building-urls }

You can construct URLs to your routes by calling `application.locations.href` with
an instance of a class annotated with `@Location`:

```kotlin
val path = application.locations.href(Listing(name = "movies", page = 10, count = 20))
```

So for this class, `path` would be `"/list/movies?page=10&count=20""`.

```kotlin
@Location("/list/{name}") data class Listing(val name: String, val page: Int, val count: Int)
```

If you construct the URLs like this, and you decide to change the format of the URL,
you will just have to update the `@Location` path, which is really convenient.

## Subroutes with parameters
{: #subroutes }

You have to create classes referencing to another class annotated with `@Location` like this, and register them normally:

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
 
To obtain parameters defined in the superior locations, you just have to include
those property names in your classes for the internal routes. For example:

```kotlin
@Location("/type/{name}") data class Type(val name: String) {
    @Location("/edit") data class Edit(val type: Type)
    @Location("/list/{page}") data class List(val type: Type, val page: Int)
}
```