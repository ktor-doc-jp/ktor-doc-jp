---
title: WebSockets
caption: WebSockets
category: servers
permalink: /servers/features/websockets.html
feature:
  artifact: io.ktor:ktor-websockets:$ktor_version
  class: io.ktor.websocket.WebSockets
redirect_from:
- /features/websockets.html
ktor_version_review: 1.0.0
---

Ktor で WebSocket 通信を行う場合、 WebSocket feature を利用します。
WebSocket はサーバとクライアント間にて順序付き双方向通信をし続けるためのメカニズムです。
チャンネルでの各メッセージは Frame と呼ばれます。
Frame の種類はテキストメッセージ、バイナリメッセージ、クローズ (接続断)、 ping 、 pong です。
Frame には final または incomplete マークを付与できます。

{% include feature.html %}

**目次:**

* 目次
{:toc}

## インストール
{: #installing}

WebSocket feature をインストールすることで WebSocket 通信ができるようになります。

```kotlin
install(WebSockets)
```

必要に応じて、インストール時にパラメータを指定します。

```kotlin
install(WebSockets) {
    pingPeriod = Duration.ofSeconds(60) // デフォルトでは disable (null)
    timeout = Duration.ofSeconds(15)
    // disable する場合は最大値 Long.MAX_VALUE を指定
    // frame がこのサイズを超えるとコネクションが切断される
    maxFrameSize = Long.MAX_VALUE 
    masking = false
}
```

## 使い方
{: #usage}

インストールすると、 [routing](/servers/features/routing.html) feature に WebSocket 用のルーティング `webSocket` を追加できるようになります。

通常のルーティングハンドラは短命なオブジェクトであることに対し、 WebSocket 用のハンドラは長命なオブジェクトになります。
また、 WebSocket 用のハンドラに関与する WebSocket のメソッドは一時中断されるため、メッセージの送受信をブロックしないよう中断されます。

`webSocket` メソッドは引数に [WebSocketSession](#WebSocketSession) のインスタンスを受け取る関数を取ります。
このインタフェースは `ReceiveChannel` 型の `incoming` と `SendChannel` 型の `outgoing` の2つのプロパティや `close` メソッドが定義されています。
詳細は [WebSocketSession](#WebSocketSession) を参照してください。


### suspend actor としての使い方
{: #actor}

```kotlin
routing {
    webSocket("/") { // websocketSession
        for (frame in incoming) {
            when (frame) {
                is Frame.Text -> {
                    val text = frame.readText()
                    outgoing.send(Frame.Text("YOU SAID: $text"))
                    if (text.equals("bye", ignoreCase = true)) {
                        close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                    }
                }
            }
        }
    }
}
```

クライアントが明示的に接続をクローズした場合、または TCP ソケット通信がクローズされた場合、 Frame の受信中に例外が投げられます。
そのため、 `while (true)` で無限ループしても、リソースリークは発生しません。
{: .note}

### チャンネルの使い方
{: #channel}

`incoming` プロパティは `ReceiveChannel` 型なので、 stream インターフェイスのような扱い方ができます。

```kotlin
routing {
    webSocket("/") { // websocketSession
        for (frame in incoming.mapNotNull { it as? Frame.Text }) {
            val text = frame.readText()
            outgoing.send(Frame.Text("YOU SAID $text"))
            if (text.equals("bye", ignoreCase = true)) {
                close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
            }
        }
    }
}
``` 

## インタフェース
{: #interface}

### WebSocketSession インタフェース
{: #WebSocketSession}

You receive a WebSocketSession as the receiver (this), giving you direct access
to these members inside your webSocket handler.

`webSocket` ハンドラ内では、 WebSocketSession を `this` として受け取り、各メンバ変数や関数に直接アクセスすることができます。

```kotlin
interface WebSocketSession {
    // Basic interface
    val incoming: ReceiveChannel<Frame> // Incoming frames channel
    val outgoing: SendChannel<Frame> // Outgoing frames channel
    fun close(reason: CloseReason)

    // `outgoing.send(frame)` と等価の関数
    // outgoing キューがいっぱいになると中断される、 enqueue frame
    // outgoing チャンネルがクローズ済の場合は例外が送出されるため、メッセージを転送する用途には利用できない
    suspend fun send(frame: Frame)

    // 呼び出しとコンテキスト
    val call: ApplicationCall
    val application: Application

    // リクエストごとのプロパティで、変更可能
    // 初期値は feature の設定から指定される
    var pingInterval: Duration?
    var timeout: Duration
    var masking: Boolean // Random XOR Mask による出力メッセージのマスキングの有効化/無効化
    var maxFrameSize: Long // フレームサイズの上限値; 上限を超えた場合は接続がクローズされる
    
    // 発展的なプロパティ
    val closeReason: Deferred<CloseReason?>
    // すべての未処理のメッセージをフラッシュし、これまでに送信されたメッセージがすべて書き出されるまで中断する
    // この関数はいつでも呼び出し可能 (クローズ後でも可能)
    // 接続がクローズされている場合は、この関数はすぐに return される
    suspend fun flush()
    // すぐに接続をクローズする
    // クローズ処理の完了は非同期で実行される
    fun terminate()
}
```

クライアントの IP など接続に関する情報が必要な場合は、 `call` プロパティを参照します。
例えば、 `websocket` ブロック内で `call.request.origin.host` のように参照します。
{: .note}

### Frame インタフェース
{: #Frame}

Frame は WebSocket プロトコルで送受信されるパケットです。
TEXT と BINARY の2種類のメッセージタイプがあります。
また、 CLOSE 、 PING 、 PONG の3種類の制御用パケットがあります。
各パケットは `buffer` というペイロードを持ちます。
また、 TEXT や CLOSE では `readText` や `readReason` メソッドを用いることでその `buffer` 内のメッセージを取得できます。

```kotlin
enum class FrameType { TEXT, BINARY, CLOSE, PING, PONG }
```

```kotlin
sealed class Frame {
    val fin: Boolean // この Frame が final かどうか
    val frameType: FrameType // この Frame の種別
    val buffer: ByteBuffer // ペイロード
    val disposableHandle: DisposableHandle

    class Binary : Frame
    class Text : Frame {
        fun readText(): String
    }
    class Close : Frame {
        fun readReason(): CloseReason?
    }
    class Ping : Frame
    class Pong : Frame
}
```

## テスト
{: #testing}

WebSocket 通信は `withTestApplication` ブロック内にて `handleWebSocketConversation` メソッドを用いることでテストできます。

{% capture test-kt %}
```kotlin
class MyAppTest {
    @Test
    fun testConversation() {
        withTestApplication {
            application.install(WebSockets)
    
            val received = arrayListOf<String>()
            application.routing {
                webSocket("/echo") {
                    try {
                        while (true) {
                            val text = (incoming.receive() as Frame.Text).readText()
                            received += text
                            outgoing.send(Frame.Text(text))
                        }
                    } catch (e: ClosedReceiveChannelException) {
                        // Do nothing!
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
            }
    
            handleWebSocketConversation("/echo") { incoming, outgoing ->
                val textMessages = listOf("HELLO", "WORLD")
                for (msg in textMessages) {
                    outgoing.send(Frame.Text(msg))
                    assertEquals(msg, (incoming.receive() as Frame.Text).readText())
                }
                assertEquals(textMessages, received)
            }
        }
    }
}
```
{% endcapture %}

{% include tabbed-code.html
    tab1-title="test.kt" tab1-content=test-kt
%}

## FAQ

### 標準的なイベント : `onConnect` 、 `onMessage` 、 `onClose` 、 `onError`
{: #standard-events}

Ktor において [WebSocket API における標準的なイベント](https://developer.mozilla.org/ja/docs/Web/API/WebSockets_API) はどのように対応付けられていますか?

* `onConnect` : `webSocket` ブロックの最初に発生
* `onMessage` : メッセージの読み取り (`incoming.receive()` など) に成功した場合、または `for (frame in incoming)` にて中断関数がイテレーションされた後に発生
* `onClose` : `incoming` チャンネルがクローズされた際に発生  
中断関数のイテレーションが完了した後か、メッセージを受信しようとした際に `ClosedReceiveChannelException` が発生した場合がそれに相当する
* `onError` : 他の例外と同じ

`onClose` と `onError` はともに、 [`closeReason` プロパティ](https://api.ktor.io/1.0.0-beta-1/io.ktor.http.cio.websocket/-default-web-socket-session/close-reason.html)がセットされます。

具体的には下記のようになります。

```kotlin
webSocket("/echo") {
    println("onConnect")
    try {
        for (frame in incoming){
            val text = (frame as Frame.Text).readText()
            println("onMessage")
            received += text
            outgoing.send(Frame.Text(text))
        }
    } catch (e: ClosedReceiveChannelException) {
        println("onClose ${closeReason.await()}")
    } catch (e: Throwable) {
        println("onError ${closeReason.await()}")
        e.printStackTrace()
    }
}
```

このサンプルでは、 `ClosedReceiveChannelException` または他の例外が発生した場合のみ、無限ループが発生します。
