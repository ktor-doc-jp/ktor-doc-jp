---
title: WebSocket
category: clients
permalink: /clients/websockets.html
caption: WebSocket
feature:
    artifact: io.ktor:ktor-client-websockets:$ktor_version,io.ktor:ktor-client-cio:$ktor_version,io.ktor:ktor-client-js:$ktor_version,io.ktor:ktor-client-okhttp:$ktor_version
    class: io.ktor.client.features.websocket.WebSockets
ktor_version_review: 1.2.0
---

{% include feature.html %}


Ktorは、CIO、OkHttp、Jsのエンジン用のWebSocketクライアントを提供します。
サーバー側に関する詳細情報を取得するには、この[セクション](/servers/features/websockets.html)に従ってください。

接続されると、クライアントとサーバーのWebSocketは通信のために同じ[WebSocketSession](/servers/features/websockets.html#WebSocketSession)インターフェイスを共有します。

WebSocketをサポートするHTTPクライアントを作成する基本的な使用法は非常に簡単です。

```kotlin
val client = HttpClient {
    install(WebSockets)
}
```

作成したら、`WebSocketSession`を開始してリクエストを実行できます。

```kotlin
client.ws(
    method = HttpMethod.Get,
    host = "127.0.0.1",
    port = 8080, path = "/route/path/to/ws"
) { // this: DefaultClientWebSocketSession

    // Send text frame.
    send("Hello, Text frame")

    // Send text frame.
    send(Frame.Text("Hello World"))

    // Send binary frame.
    send(Frame.Binary(...))

    // Receive frame.
    val frame = incoming.receive()
    when (frame) {
        is Frame.Text -> println(frame.readText())
        is Frame.Binary -> println(frame.readBytes())
    }
}
```

WebSocketSessionの詳細については、[WebSocketSessionページ](/servers/features/websockets.html#WebSocketSession)と[APIリファレンス](https://api.ktor.io/{{ site.ktor_version }}/io.ktor.client.features.websocket/)を確認してください。

