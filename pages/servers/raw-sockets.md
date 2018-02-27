---
title: Sockets
section: Servers
permalink: /servers/raw-sockets.html
caption: Raw Sockets  
---

In addition to HTTP handling for the [server](/servers/application.html) and the [client](/clients/http-client.html), Ktor supports client and server, TCP and UDP raw sockets.
It exposes a suspending API that uses NIO under the hoods.

**Table of contents:**

* TOC
{:toc}

## Sockets

This functionality is exposed through the `io.ktor:ktor-network:$ktor_version` artifact.
{: .note.artifact }

In order to create sockets, either server or client sockets, you have to use the `aSocket` builder,
with an optional `ActorSelectorManager`: `aSocket(selector)` or just `aSocket()`.

Then use:

* `val socketBuilder = aSocket().tcp()` for a builder using TCP sockets
* `val socketBuilder = aSocket().udp()` for a builder using UDP sockets

This returns a `SocketBuilder` that can be used to:
 
* `val serverSocket = aSocket().tcp().bind(address)` to listen to an address (for servers)
* `val clientSocket = aSocket().tcp().connect(address)` to connect to an address (for clients)
 
If you need to control the dispatcher used by the sockets, you can instantiate a selector,
that uses, for example, a cached thread pool:
```kotlin
val exec = Executors.newCachedThreadPool()
val selector = ActorSelectorManager(exec.asCoroutineDispatcher())
val tcpSocketBuilder = aSocket(selector).tcp()
```

Once you have a `socket` open by either [binding](#server) or [connecting](#client) the builder,
you can read from or to write to the socket, by opening read/write channels:

```kotlin
val input : ByteReadChannel  = socket.openReadChannel()
val output: ByteWriteChannel = socket.openWriteChannel(autoFlush = true)
```

You can read the KDoc for [ByteReadChannel](https://github.com/Kotlin/kotlinx.coroutines/blob/master/core/kotlinx-coroutines-io/src/main/kotlin/kotlinx/coroutines/experimental/io/ByteReadChannel.kt)
and [ByteWriteChannel](https://github.com/Kotlin/kotlinx.coroutines/blob/master/core/kotlinx-coroutines-io/src/main/kotlin/kotlinx/coroutines/experimental/io/ByteWriteChannel.kt)
for further information on available methods.
{: .note}

## Server

When creating a socket server, you have to `bind` to a specific `SocketAddress` to get
a `ServerSocket`:

```kotlin
val server = aSocket(selector).tcp().bind(InetSocketAddress("127.0.0.1", 2323))
```

The server socket has an `accept` method that returns, one at a time, 
a connected socket for each incoming connection pending in the *backlog*:

```kotlin
val socket = server.accept()
```

If you want to support multiple clients at once, remember to call `launch { }` to prevent
suspending the function that is accepting the sockets.
{: .note}

*Simple Echo Server*:

```kotlin
fun main(args: Array<String>) {
    runBlocking {
        val server = aSocket().tcp().bind(InetSocketAddress("127.0.0.1", 2323))
        println("Started echo telnet server at ${server.localAddress}")
        
        while (true) {
            val socket = server.accept()
            
            launch {
                println("Socket accepted: ${socket.remoteAddress}")
                
                val input = socket.openReadChannel()
                val output = socket.openWriteChannel(autoFlush = true)
                
                try {
                    while (true) {
                        val line = input.readASCIILine()
                        
                        println("${socket.remoteAddress}: $line")
                        output.writeBytes("$line\r\n")
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                    socket.close()
                }
            }
        }
    }
}
```
{: .compact}

Then you can connect to it using *telnet* and start typing:

```
telnet 127.0.0.1 2323
```

For each line that you type (you have to press the return key), the server will reply
with the same line:


```
Trying 127.0.0.1...
Connected to 127.0.0.1
Escape character is '^]'.

Hello
Hello
World
World
|
``` 

## Client

When creating a socket client, you have to `connect` to a specific `SocketAddress` to get
a `Socket`:

```kotlin
val socket = aSocket().tcp().connect(InetSocketAddress("127.0.0.1", 2323))
```

*Simple Client Connecting to an Echo Server:*

```kotlin
fun main(args: Array<String>) {
    runBlocking {
        val socket = aSocket().tcp().connect(InetSocketAddress("127.0.0.1", 2323))
        val input = socket.openReadChannel()
        val output = socket.openWriteChannel(autoFlush = true)

        output.writeBytes("hello\r\n")
        val response = input.readASCIILine()
        println("Server said: '$response'")
    }
}
```

