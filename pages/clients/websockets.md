---
title: WebSockets
section: Clients
permalink: /clients/websockets.html
caption: WebSockets  
feature:
    artifact: io.ktor:ktor-client-websocket:$ktor_version
    class: io.ktor.client.features.websocket.WebSockets
---

Ktor provides a WebSocket client in addition to supporting [WebSockets at server side](/features/websockets.html). 

Once connected, client and server WebSockets share the same [WebSocketSession](/features/websockets.html#WebSocketSession)
interface for communication.

Right now, client WebSockets are only available for the CIO Client Engine.

The basic usage to create a http client supporting WebSockets is pretty simple:

```kotlin
val client = HttpClient(CIO).config { install(WebSockets) }
```

Once created we can perform a request, starting a WebSocketSession:

```kotlin
client.ws(method = HttpMethod.Get, host = "127.0.0.1", port = 8080, path = "/route/path/to/ws") { // this: WebSocketSession
    send(Frame.Text("Hello World"))

    for (message in incoming.map { it as? Frame.Text }.filterNotNull()) {
        println(message.readText())
    }
}
```

