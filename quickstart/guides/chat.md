---
title: Chat
caption: "Guides: How to implement a chat with WebSockets"
category: quickstart
---

In this tutorial, you will learn to make a Chat application using Ktor.
We are going to use WebSockets for a real-time bidirectional communication.

This is an advanced tutorial and you are required to have some basic concepts about Ktor,
so first you should follow the [guide about making a Website](/quickstart/guides/website.html).

To achieve this, we are going to use the [Routing], [WebSockets] and [Sessions] features.

[Routing]: /servers/features/routing.html
[WebSockets]: /servers/features/websockets.html
[Sessions]: /servers/features/sessions.html

**Table of contents:**

* TOC
{:toc}

## Setting up the project

The first step is to set up a project. You can follow the [Quick Start](/quickstart/index.html) guide,
or use the following form to create one:

{% include preconfigured-form.html hash="dependency=ktor-sessions&dependency=routing&dependency=ktor-websockets&artifact-name=chat" %}

## Understanding WebSockets

WebSockets is a subprotocol of HTTP. It starts as a normal HTTP request with an upgrade request header,
and the connection switches to be a bidirectional communication instead of a request response one.

The smallest unit of transmission that can be sent as part of the WebSocket protocol, is a `Frame`.
For a single message, TCP can be fragmented in several packets. A WebSocket Frame defines a type and a length,
thus could be transmitted in several TCP packets, but will be reassembled into a single Frame.

You can see Frames as WebSocket messages. Frames could be the following types: text, binary, close, ping and pong.

You will normally handle `Text` and `Binary` frames, and the other will be handled by Ktor in most of the cases
(though you can use a raw mode).

In its page, you can read more about the [WebSockets feature](/servers/features/websockets.html).  

## WebSocket route

This first step is to create a route for the WebSocket. In this case we are going to define the `/chat` route.
We are going to start with an echo WebSocket route, that will send you back the same messages that you send to it.

`webSocket` routes are intended to be long-lived. Since it is a suspend block and uses lightweight Kotlin coroutines,
it is fine and you can handle (depending on the machine and the complexity) hundreds of thousands of connections
at once, while keeping your code easy to read and to write.

```kotlin
routing {
    webSocket("/chat") { // this: DefaultWebSocketSession
        while (true) {
            val frame = incoming.receive()
            when (frame) {
                is Frame.Text -> {
                    val text = frame.readText()
                    outgoing.send(Frame.Text(text))
                }
            }
        }
    }
}
```

## Keeping a set of opened connections

We can use a Set to keep a list of opened connections. We can use a plain `try...finally` to keep track of them.
Since Ktor is multithreaded by default we should use thread-safe collections or [limit the body to a single thread with newSingleThreadContext](https://github.com/Kotlin/kotlinx.coroutines/blob/master/coroutines-guide.md#coroutine-context-and-dispatchers){:target="_blank"}. 

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

## Propagating a message among all the connections

Now that we have a set of connections, we can iterate over them and use the session
to send the frames we need.
Everytime a user send a message, we are going to propagate to all the connected clients.

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
                        // Iterate over all the connections
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

## Assigning names to users/connections

We might want to associate some information, like a name to an oppened connection,
we can create a object that includes the WebSocketSession and store it instead
like this:

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

## Exercises

### Creating a client

Create a JavaScript client connecting to this endpoint and serve it with ktor.

### JSON

Use [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) to send and receive VOs

