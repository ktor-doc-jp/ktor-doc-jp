---
title: コンテントネゴシエーション
caption: Content-TypeとAccept Headerをベースにしたコンテントネゴシエーション
category: servers
permalink: /servers/features/content-negotiation.html
feature:
  artifact: io.ktor:ktor-server-core:$ktor_version
  class: io.ktor.features.ContentNegotiation
children: /servers/features/content-negotiation/
redirect_from:
- /features/content-negotiation.html
ktor_version_review: 1.0.0
---

この機能は、`Content-Type`と`Accept`ヘッダーに応じて自動でのコンテンツの変換を行う機能です。

{% include feature.html %}

## 基本的な使い方
{: #basic}

ContentNegotiation Featureを使うとカスタムのコンバーターを登録・設定することができます。

```kotlin
install(ContentNegotiation) {
    register(MyContentType, MyContentTypeConverter()) {
        // Optionally configure the converter...
    }
}
```

例えば:

```kotlin
install(ContentNegotiation) {
    register(ContentType.Application.Json, JacksonConverter())
}
```

## 送信時
{: #sending }

直接扱うことができないオブジェクトでレスポンスを返したいとき、例えばカスタムのデータクラスを使うとき、
この機能はクライアントの`Accept`ヘッダーをチェックし、
どの`Content-Type`が利用されるかと、そしてどの`ContentConverter`が呼ばれるかを決定します。

```kotlin
call.respond(MyDataClass("hello", "world"))
```

たった今、送信時にサポートされている唯一のコンテントネゴシエーションの戦略は、クライアントの`Accept`ヘッダーを使うものです。
[その他の戦略を実装する上では問題](https://github.com/ktorio/ktor/issues/357)があります。
{: .note} 

## 受信時
{: #receiving }

受信時にはリクエストの`Content-Type`が使われ、どの`ContentConverter`がリクエストを処理するのに使われるのかが決定されます。

```kotlin
val myDataClass = call.receive<MyDataClass>()
```

## ContentConverterインターフェース
{: #content-converter}

自身でコンバーターを書きたいのなら、`ContentConverter`インターフェースを実装する必要があります。

```kotlin
interface ContentConverter {
    suspend fun convertForSend(context: PipelineContext<Any, ApplicationCall>, contentType: ContentType, value: Any): Any?
    suspend fun convertForReceive(context: PipelineContext<ApplicationReceiveRequest, ApplicationCall>): Any?
}
```

例えば、`GsonConverter`の実装は以下のようになっています:

```kotlin
class GsonConverter(private val gson: Gson = Gson()) : ContentConverter {
    override suspend fun convertForSend(context: PipelineContext<Any, ApplicationCall>, contentType: ContentType, value: Any): Any? {
        return TextContent(gson.toJson(value), contentType.withCharset(context.call.suitableCharset()))
    }

    override suspend fun convertForReceive(context: PipelineContext<ApplicationReceiveRequest, ApplicationCall>): Any? {
        val request = context.subject
        val channel = request.value as? ByteReadChannel ?: return null
        val reader = channel.readRemaining().readText((context.call.request.contentCharset() ?: Charsets.UTF_8).newDecoder()).reader()
        return gson.fromJson(reader, request.type.javaObjectType)
    }
}
```

## 初めから利用可能なContentConverter
{: #available-converters}

Ktorはいくつかの初めから利用可能なコンテントコンバーターを提供しています。

* `application/json`
    * [GsonConverter](/servers/features/content-negotiation/gson.html)
    * [JacksonConverter](/servers/features/content-negotiation/jackson.html)
    * [SerializationConverter](/servers/features/content-negotiation/serialization-converter.html)

