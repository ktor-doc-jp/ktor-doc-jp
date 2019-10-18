---
title: Data Conversion
caption: Data Conversion
category: servers
permalink: /servers/features/data-conversion.html
feature:
  artifact: io.ktor:ktor-server-core:$ktor_version
  class: io.ktor.features.DataConversion
redirect_from:
- /features/data-conversion.html
ktor_version_review: 1.0.0
---

`DataConversion` feature は値のリストをシリアライズおよびデシリアライズすることができます。

プリミティブ型や列挙型はデフォルトで処理できますが、 `DataConversion` を用いることで、他の型も処理できるようになります。

[Locations feature](/servers/features/locations.html) を使用していて、かつパラメータの一部をプリミティブ型や列挙型以外の
型に変換したい場合、この feature を用いて自作のコンバータを作成することができます。


**目次:**

* 目次
{:toc}

{% include feature.html %}

## インストール
{: #basic-installation }

DataConversion のインストールは簡単です。
プリミティブ型の変換はデフォルトで対応済です。

```kotlin
install(DataConversion)
```

## コンバータの追加
{: #adding-converters }

DataConversion は、型変換を定義するための `convert<T>` メソッドを提供します。
このメソッド内で、デコード処理を行う `decode` コールバック関数と
エンコード処理を行う `encode` コールバック関数を定義します。

* デコードコールバック : `converter: (values: List<String>, type: Type) -> Any?`
  String 型のリスト `values` と Type 型の `type` を引数に取ります。
  `values` は URL 内で繰り返し使用されうる値です。 (例 : `a=1&a=b`)
  `type` はコンバート先の型を指定します。
  この関数はデコードした値を返却する必要があります。
* エンコードコールバック : `converter: (value: Any?) -> List<String>`
  任意の値を引数に取り、文字列のリストを返します。
  要素数が1のリストを返す場合は `key=item1` のようにシリアライズされます。
  複数の要素がある場合は、 `samekey=item1&samekey=item2` のようなクエリ文字列にシリアライズされます。
  

例:

```kotlin
install(DataConversion) {
    convert<Date> { // this: DelegatingConversionService
        val format = SimpleDateFormat.getInstance()
    
        decode { values, _ -> // converter: (values: List<String>, type: Type) -> Any?
            values.singleOrNull()?.let { format.parse(it) }
        }

        encode { value -> // converter: (value: Any?) -> List<String>
            when (value) {
                null -> listOf()
                is Date -> listOf(SimpleDateFormat.getInstance().format(value))
                else -> throw DataConversionException("Cannot convert $value as Date")
            }
        }
    }
}
```

他の用途の一つとして、列挙型へのシリアライズ化の方法を独自定義することが挙げられます。
デフォルトでは大文字と小文字は区別され、 `.name` を用いてシリアライズ / デシリアライズされます。
一方で、例えばシリアライズ時は小文字で出力し、デシリアライズ時は大文字と小文字を区別したくない場合は、下記のように実装します。

```kotlin
enum class LocationEnum {
    A, B, C
}

@Location("/") class LocationWithEnum(val e: LocationEnum)

@Test fun `location class with custom enum value`() = withLocationsApplication {
    application.install(DataConversion) {
        convert(LocationEnum::class) {
            encode { if (it == null) emptyList() else listOf((it as LocationEnum).name.toLowerCase()) }
            decode { values, type -> LocationEnum.values().first { it.name.toLowerCase() in values } }
        }
    }
    application.routing {
        get<LocationWithEnum> {
            call.respondText(call.locations.resolve<LocationWithEnum>(LocationWithEnum::class, call).e.name)
        }
    }

    urlShouldBeHandled("/?e=a", "A")
    urlShouldBeHandled("/?e=b", "B")
}
```

## Accessing the Service
{: #service }

You can easily access the DataConversion service, from any call with:

```kotlin
val dataConversion = call.conversionService
```

## The ConversionService Interface
{: #interface }

```kotlin
interface ConversionService {
    fun fromValues(values: List<String>, type: Type): Any?
    fun toValues(value: Any?): List<String>
}
```
{: #ConversionService }

```kotlin
class DelegatingConversionService(private val klass: KClass<*>) : ConversionService {
    fun decode(converter: (values: List<String>, type: Type) -> Any?)
    fun encode(converter: (value: Any?) -> List<String>)
}
```
{: #DelegatingConversionService }