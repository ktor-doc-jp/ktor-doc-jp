---
title: WebSockets
category: clients
permalink: /clients/websockets.html
caption: WebSockets  
feature:
    artifact: io.ktor:ktor-client-websocket:$ktor_version
    class: io.ktor.client.features.websocket.WebSockets
---

To use this feature, you need to include `io.ktor:ktor-client-websocket` artifact.
{: .note.artifact }

Ktor provides a WebSocket client in addition to supporting [WebSockets at server side](/features/websockets.html). 

Once connected, client and server WebSockets share the same [WebSocketSession](/features/websockets.html#WebSocketSession)
interface for communication.

Right now, client WebSockets are only available for the CIO Client Engine.

The basic usage to create a http client supporting WebSockets is pretty simple:

```kotlin
val client = HttpClient(CIO).config { install(WebSockets) }
```

Once created we can perform a request, starting a `WebSocketSession`:

```kotlin
client.ws(method = HttpMethod.Get, host = "127.0.0.1", port = 8080, path = "/route/path/to/ws") { // this: WebSocketSession
    send(Frame.Text("Hello World"))

    for (message in incoming.map { it as? Frame.Text }.filterNotNull()) {
        println(message.readText())
    }
}
```

You can configure timeout and ping periods by casting to `DefaultWebSocketSession` (next version won't require this):

```kotlin
client.ws(...) { // this: WebSocketSession
    (this as DefaultWebSocketSession).timeout = Duration.ofMinutes(10)
    (this as DefaultWebSocketSession).pingInterval = Duration.ofMinutes(10) // null to disable it
}
```
