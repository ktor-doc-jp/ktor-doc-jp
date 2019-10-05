---
title: キャッシュヘッダー
caption: キャッシュヘッダーの操作
category: servers
permalink: /servers/features/caching-headers.html
feature:
  artifact: io.ktor:ktor-server-core:$ktor_version
  class: io.ktor.features.CachingHeaders
redirect_from:
- /features/caching-headers.html
ktor_version_review: 1.0.0
---

CachingOptions機能を使うと、`Cache-Control`と`Expires`ヘッダーを送信し、
クライアントやプロキシにリクエストを簡単にキャッシュさせることができます。

{% include feature.html %}

その他多くのFeatureと同じように、基本的な機能は初めからインストールされます。
しかし何かしらを行おうと思った場合は、
以下の例のようにoutputContentをCachingOptionsに変換する`options`ブロックを定義する必要があります:

```kotlin
install(CachingHeaders) {
    options { outgoingContent ->
        when (outgoingContent.contentType?.withoutParameters()) {
            ContentType.Text.CSS -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60))
            else -> null
        }
    }
}
```

`options`設定メソッドを使うと、引数として与えられた`outgoingContent: OutgoingContent`から、
オプショナルで`CachingOptions`を指定するコードを定義できます。
例えば、送信メッセージの`Content-Type`を使い、どのCache-Controlが使われるのかを決めることができます。

## CachingOptions, CacheControl

`options`高階関数を使うとき`CachingOption`を返すことで、`CacheControl`とあとオプションとして有効期限も示す必要があります:

```kotlin
data class CachingOptions(val cacheControl: CacheControl? = null, val expires: ZonedDateTime? = null)

sealed class CacheControl(val visibility: Visibility?) {
    enum class Visibility { Public, Private }
    
    class NoCache(visibility: Visibility?) : CacheControl(visibility)
    class NoStore(visibility: Visibility?) : CacheControl(visibility)
    class MaxAge(val maxAgeSeconds: Int, val proxyMaxAgeSeconds: Int? = null, val mustRevalidate: Boolean = false, val proxyRevalidate: Boolean = false, visibility: Visibility? = null) : CacheControl(visibility)
}
```

いくつかのOptionがある場合、マッチしたOptionごとに`Cache-Control`ヘッダーを付与します。
