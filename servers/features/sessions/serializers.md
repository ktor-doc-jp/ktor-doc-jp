---
title: シリアライザー
caption: セッションシリアライザー
category: servers
redirect_from:
- /features/sessions/serializers.html
ktor_version_review: 1.0.0
---

## シリアライザー
{: #serializer}

カスタムのシリアライザーを以下のように指定できます:

```kotlin
application.install(Sessions) {
    cookie<MySession>("SESSION") {
        serializer = MyCustomSerializer()
    }
} 
```

どのシリアライザも指定しない場合、内部的な最適化されたフォーマットが利用されます。

### SessionSerializerReflection
{: #SessionSerializerReflection}

これはどのシリアライザも指定されなかった場合のデフォルトのシリアライザです:

```kotlin
cookie<MySession>("SESSION") {
    serializer = autoSerializerOf()
}
```

## カスタムシリアライザ
{: #extending-serializers}

Sessions APIは`SessionSerializer`インターフェースを提供し、以下のようになります:

```kotlin
interface SessionSerializer {
    fun serialize(session: Any): String
    fun deserialize(text: String): Any
}
```

このインターフェースは汎用的なシリアライザ用であり、以下のようにインストールできます:

```kotlin
cookie<MySession>("NAME") {
    serializer = MyCustomSerializer()
}
```

そのため例えば、Gsonを使ったJSONセッションシリアライザを作成することができます:

```kotlin
class GsonSessionSerializer(
    val type: java.lang.reflect.Type, val gson: Gson = Gson(), configure: Gson.() -> Unit = {}
) : SessionSerializer {
    init {
        configure(gson)
    }

    override fun serialize(session: Any): String = gson.toJson(session)
    override fun deserialize(text: String): Any = gson.fromJson(text, type)
}
```

そして設定できます:

```kotlin
cookie<MySession>("NAME") {
    serializer = GsonSessionSerializer(MySession::class.java)
}
```

