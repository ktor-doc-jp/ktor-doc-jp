---
title: ストレージ
caption: セッションストレージ
category: servers
redirect_from:
- /features/sessions/storages.html
ktor_version_review: 1.0.0
---

`SessionStorageMemory`、`DirectoryStorage`という事前に定義された2つのストレージがあります。
そして他の組み合わせ可能なストレージとして`CacheStorage`があります。

`DirectoryStorage`と`CacheStorage`は`io.ktor:ktor-server-sessions:$ktor_version`アーティファクトに依存しています。
{: .note.artifact } 

このモードでは、実際のセッションコンテンツの代わりにセッションIDを送信します。
このIDは、特定の`SessionStorage`を使ってコンテンツをサーバサイドに保存するため使われます。
このモードは`cookie`または`header`メソッド内でストレージを第二引数で指定することで使えます。

例:

```kotlin
install(Sessions) {
    cookie<SampleSession>("SESSION_FEATURE_SESSION_ID", SessionStorageMemory()) {
        cookie.path = "/"
    }
}
```

## カスタムSessionStorage

`SessionStorage`はセッションペイロードの参照・保存の責務をもちます。
インターフェースは*suspend*可能になっており、データを非同期に変換することができます。

データはストリームとして変換され、呼び出し先はバイナリペイロードを提供するコンシューマとプロバイダを渡す必要があります。
呼び出し先はバイトチャネルのオープン・クローズの責務をもちます。

```kotlin
interface SessionStorage {
    suspend fun write(id: String, provider: suspend (ByteWriteChannel) -> Unit)
    suspend fun invalidate(id: String)
    suspend fun <R> read(id: String, consumer: suspend (ByteReadChannel) -> R): R
}
```

もしストレージが情報をストリームとして保存するよい方法を提供していなければ、
`ByteArray`を使って単純に読み書きするようなシンプルなアダプターを使うこともできます。
また、プリミティブなストリームベースバージョンでAPIを処理する方法を知るための例として使用することもできます。

{% capture simplified-session-storage-sample-kt %}{% include simplified-session-storage-sample.md %}{% endcapture %}

{% include tabbed-code.html
    tab1-title="SimplifiedSessionStorage.kt" tab1-content=simplified-session-storage-sample-kt
%}
