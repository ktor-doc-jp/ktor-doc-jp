---
title: Chat
caption: "ガイド: WebSocketでチャットを実装する方法"
category: quickstart
permalink: /quickstart/guides/chat.html
ktor_version_review: 1.0.0
---


このチュートリアルでは、 Ktor を用いたチャットアプリケーションの作り方を学びます。
WebSocket を用いてリアルタイム双方向通信を行います。

これを達成するために、 [Routing] 、 [WebSockets] 、 [Sessions] の3つの Feature を用います。

[Routing]: /servers/features/routing.html
[WebSockets]: /servers/features/websockets.html
[Sessions]: /servers/features/sessions.html

本チュートリアルは高度な内容になっています。
Ktor に関する基本的な知識があることを前提としているため、先に [ウェブサイトの作り方](/quickstart/guides/website.html) を
学んでください。

**目次:**

* 目次
{:toc}

## プロジェクトのセットアップ

まずはじめに、プロジェクトのセットアップを行います。
[Quick Start](/quickstart/index.html) を参考にするか、下記の設定を用いてプロジェクトを作成してください。

{% include preconfigured-form.html hash="dependency=ktor-sessions&dependency=routing&dependency=ktor-websockets&artifact-name=chat" %}

## WebSocket とは

WebSocket は HTTP のサブプロトコルです。
WebSocket 通信は Upgrade リクエストヘッダを付与した通常の HTTP リクエストから始まり、なにか1つレスポンスを返すのではなく、双方向通信に接続が切り替わります。

WebSocket プロトコルにて送信可能な情報の最小単位は `Frame` と呼ばれます。
WebSocket Frame は型、長さ、そしてバイナリかテキストのペイロードを定義します。
内部的には、これらのフレームは複数の TCP パケットに分割されて透過的に送信される場合があります。

Frame は WebSocket メッセージとして扱えます。
Frame の型は、 `Text` 、 `Binary` 、 `Close` 、 `Ping` 、 `Pong` があります。

基本的には、 `Text` と `Binary` のフレームだけを考えればよく、それ以外の種類は多くの場合は Ktor に任せることができます。
(Raw mode を使うことで、独自の Frame 型を扱うこともできます。)

詳細は [WebSockets feature](/servers/features/websockets.html) に記載してあります。

## WebSocket ルーティング

まずはじめに、 WebSocket 用のルーティングを作りましょう。
今回は `/chat` という名前にします。
`/chat` という名前にしましたが、最初のうちは受信したメッセージをオウム返しするだけの「エコー」 WebSocket ルーティングとして機能させます。

`webSocket` ルーティングは長命なオブジェクトです。
Suspend block (`CoroutineScope`) で Kotlin の軽量な coroutine を用いているので、コードの可読性を保ちつつも、
数十万のコネクションを一度に処理できます。 (マシンのスペックに依存します。)

```kotlin
routing {
    webSocket("/chat") { // this: DefaultWebSocketSession
        while (true) {
            val frame = incoming.receive() // suspend
            when (frame) {
                is Frame.Text -> {
                    val text = frame.readText()
                    outgoing.send(Frame.Text(text)) // suspend
                }
            }
        }
    }
}
```

## コネクションの保持

確立済みコネクション群を Set に保持できます。
言語標準の `try...finally` を用いてコネクションの追跡ができます。
Ktor はデフォルトでマルチスレッドで動作するため、スレッドセーフなコレクションを利用するか、
[`newSingleThreadContext` を用いて実体をシングルスレッドに制限](https://github.com/Kotlin/kotlinx.coroutines/blob/master/coroutines-guide.md#coroutine-context-and-dispatchers){:target="_blank"}
しなければなりません。

```kotlin
routing {
    val wsConnections = Collections.synchronizedSet(LinkedHashSet<DefaultWebSocketSession>())
    
    webSocket("/chat") { // this: DefaultWebSocketSession
        wsConnections += this
        try {
            while (true) {
                val frame = incoming.receive()
                // ...
            }
        } finally {
            wsConnections -= this
        }
    }
}
```

## すべてのコネクションにメッセージを伝播させる

コネクションの集合(Set)を保持できたので、全コネクションに対して送信したい frame を送信することができます。
ユーザがメッセージを送信するたびに、接続済のすべてのクライアントに対しそのメッセージを伝播させてみましょう。

```kotlin
routing {
    val wsConnections = Collections.synchronizedSet(LinkedHashSet<DefaultWebSocketSession>())
    
    webSocket("/chat") { // this: DefaultWebSocketSession
        wsConnections += this
        try {
            while (true) {
                val frame = incoming.receive()
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        // 全コネクションに対する繰り返し処理
                        for (conn in wsConnections) {
                            conn.outgoing.send(Frame.Text(text))
                        }
                    }
                }
            }
        } finally {
            wsConnections -= this
        }
    }
}
```

## users や connections に名前をつける

接続済みのコネクションに名前をつけるなど、何らかの情報を付与したくなることがあります。
下記のように、 WebSocketSession となにかを一緒に情報を保持することができます。

```kotlin
class ChatClient(val session: DefaultWebSocketSession) {
    companion object { var lastId = AtomicInteger(0) }
    val id = lastId.getAndIncrement()
    val name = "user$id"
}

routing {
    val clients = Collections.synchronizedSet(LinkedHashSet<ChatClient>())
    
    webSocket("/chat") { // this: DefaultWebSocketSession
        val client = ChatClient(this)
        clients += client
        try {
            while (true) {
                val frame = incoming.receive()
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        // Iterate over all the connections
                        val textToSend = "${client.name} said: $text"
                        for (other in clients.toList()) {
                            other.session.outgoing.send(Frame.Text(textToSend))
                        }
                    }
                }
            }
        } finally {
            clients -= client
        }
    }
}
```

## 発展課題

### クライアントの作成

作成したエンドポイントに接続する JavaScript クライアントを作成し、Ktor を用いて提供しましょう。

### JSON

[kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) を用いて Value Object の送受信をしましょう。

