---
title: Cookie/Header
caption: Cookie/Headerセッション
category: servers
redirect_from:
- /features/sessions/cookie-header.html
ktor_version_review: 1.0.0
---

セッションとして、Cookieを使うこともカスタムのHTTPヘッダーを使うこともできます。
コードは大体同じですが、どこにセッション情報を送信したいかによって、`cookie`か`header`メソッドのどちらかを呼び出す必要があります。

## Cookie vs ヘッダー セッション
{: #cookies-headers }

cookieまたはヘッダーを使い、sessionIDまたはペイロードを変換する際、どの方式をとりたいかはコンシューマによります。
例えば、ウェブサイトの場合、通常はCookieを使い、APIにおいてはヘッダーを使いたいかもしれません。

Sessions.Configurationは`cookie`と`header`の2つのメソッドを提供し、セッションをどう変換するかを選択できます: 

### Cookie

```kotlin
application.install(Sessions) {
    cookie<MySession>("SESSION")
} 
```

追加のブロックを提供することでCookieの設定ができます。
例えば[SameSite拡張](https://caniuse.com/#search=samesite)の追加のようなCookieプロパティの設定ができます:

```kotlin
application.install(Sessions) {
    cookie<MySession>("SESSION") {
        cookie.extensions["SameSite"] = "lax"
    }
} 
```

Cookieメソッドはブラウザセッションを想定しています。
標準的な[`Set-Cookie` header](https://developer.mozilla.org/es/docs/Web/HTTP/Headers/Set-Cookie)が使われます。
cookieブロック内では、`Set-Cookie`ヘッダーを設定するような`cookie`プロパティにアクセスすることができます。
例えばcookieの`path`や有効期限やドメインやhttpsに関わることなどを設定できます。

```kotlin
install(Sessions) {
    cookie<SampleSession>("COOKIE_NAME") {
        cookie.path = "/"
        /* ... */
    }
}
```

### Header

Header関数はAPI用途を想定しています。
JavaScript XHRリクエストと、サーバサイドからのリクエストの療法を想定しています。
APIクライアントにとっては、cookieを操作するよりも、カスタムのヘッダーを読み書きするほうが通常簡単です。

```kotlin
install(Sessions) {
    header<SampleSession>("HTTP_HEADER_NAME") { /* ... */ }
}
```

```kotlin
application.install(Sessions) {
    header<MySession>("SESSION")
} 
```

## カスタムストレージ
{: #extending-storages}

Session APIは`SessionStorage`インターフェースを提供し、以下のようになっています:

```kotlin
interface SessionStorage {
    suspend fun write(id: String, provider: suspend (ByteWriteChannel) -> Unit)
    suspend fun invalidate(id: String)
    suspend fun <R> read(id: String, consumer: suspend (ByteReadChannel) -> R): R
}
```

3つの関数すべて`suspend`でマークされており、完全に非同期になるよう設計されています。
そして非同期Channelから読み書きできるAPIを提供する`kotlinx.coroutines.io`から、`ByteWriteChannel`と`ByteReadChannel`を利用しています。

実装において、ByteWriteChannelとByteReadChannelを提供するcallbackを呼びだす必要があります。
ByteWriteChannelとByteReadChannelをあなたは提供する必要があり、オープン・クローズするのはあなたの責務です。
`ByteWriteChannel`と`ByteReadChannel`についてはそれらのライブラリのドキュメント内でより詳細に読めます。
単にByteArrayを読み込み・保存する必要があるだけならば、このシンプルなセッションストレージを提供するスニペットを使えます。

{% capture simplified-session-storage-sample-kt %}{% include simplified-session-storage-sample.md %}{% endcapture %}

{% include tabbed-code.html
    tab1-title="SimplifiedSessionStorage.kt" tab1-content=simplified-session-storage-sample-kt
%}


このシンプルなストレージを使い、2つのよりシンプルなメソッドを実装する必要があります:

```kotlin
abstract class SimplifiedSessionStorage : SessionStorage {
    abstract suspend fun read(id: String): ByteArray?
    abstract suspend fun write(id: String, data: ByteArray?): Unit
}
```

そのため例えば、redisセッションストレージは以下のようになります:

```kotlin
class RedisSessionStorage(val redis: Redis, val prefix: String = "session_", val ttlSeconds: Int = 3600) :
    SimplifiedSessionStorage() {
    private fun buildKey(id: String) = "$prefix$id"

    override suspend fun read(id: String): ByteArray? {
        val key = buildKey(id)
        return redis.get(key)?.unhex?.apply {
            redis.expire(key, ttlSeconds) // refresh
        }
    }

    override suspend fun write(id: String, data: ByteArray?) {
        val key = buildKey(id)
        if (data == null) {
            redis.del(buildKey(id))
        } else {
            redis.set(key, data.hex)
            redis.expire(key, ttlSeconds)
        }
    }
}
```
