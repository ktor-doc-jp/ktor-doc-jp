---
title: Sockets
section: Servers
permalink: /servers/raw-sockets.html
caption: Raw Sockets  
---

In addition to HTTP handling for the server and the client, Ktor supports raw sockets
with a suspending API.

**Table of contents:**

* TOC
{:toc}

## Sockets

This functionality is exposed through the `io.ktor:ktor-network:$ktor_version` artifact.

In order to create sockets, either server or client sockets, you have to instantiate an `ActorSelectorManager`.
Then use: `aSocket(selector).tcp()` for tcp, and `aSocket(selector).udp()` for udp sockets. 
 
```kotlin
val exec = Executors.newCachedThreadPool()
val selector = ActorSelectorManager(exec.asCoroutineDispatcher())
val tcpSocketBuilder = aSocket(selector).tcp()
```

In order to read from, or to write to the socket, you have to open read/write channels:

```kotlin
val input : ByteReadChannel  = socket.openReadChannel()
val output: ByteWriteChannel = socket.openWriteChannel(autoFlush = true)
```

You can read the KDoc for [ByteReadChannel](https://github.com/Kotlin/kotlinx.coroutines/blob/master/core/kotlinx-coroutines-io/src/main/kotlin/kotlinx/coroutines/experimental/io/ByteReadChannel.kt)
and [ByteWriteChannel](https://github.com/Kotlin/kotlinx.coroutines/blob/master/core/kotlinx-coroutines-io/src/main/kotlin/kotlinx/coroutines/experimental/io/ByteWriteChannel.kt)
for further information.

## Server

When creating a socket server, you have to `bind` to a specific `SocketAddress` to get
a `ServerSocket`:

```kotlin
val server = aSocket(selector).tcp().bind(InetSocketAddress("127.0.0.1", 2323))
```

The server socket have an `accept` method to get the connected connections:

```kotlin
val socket = server.accept()
```

**Note:** If you want to support multiple clients at once, remember to call `launch { }` to prevent
suspending the function that is accepting the sockets.
{: .note}

**Simple echo server:**

```kotlin
fun main(args: Array<String>) {
    runBlocking {
        val exec = Executors.newCachedThreadPool()
        val selector = ActorSelectorManager(exec.asCoroutineDispatcher())
        val server = aSocket(selector).tcp().bind(InetSocketAddress("127.0.0.1", 2323))
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

Then you can connect to it using telnet and start typing.

```
telnet 127.0.0.1 2323
```

Each line –every time you press the return key–, it should reply with your line:

## Client

When creating a socket client, you have to `connect` to a specific `SocketAddress` to get
a `Socket`:

```kotlin
val socket = aSocket(selector).tcp().connect(InetSocketAddress("127.0.0.1", 2323))
```

**Simple client connecting to echo server:**

```kotlin
fun main(args: Array<String>) {
    runBlocking {
        val exec = Executors.newCachedThreadPool()
        val selector = ActorSelectorManager(exec.asCoroutineDispatcher())
        val socket = aSocket(selector).tcp().connect(InetSocketAddress("127.0.0.1", 2323))
        val input = socket.openReadChannel()
        val output = socket.openWriteChannel(autoFlush = true)
        socket.writeBytes("hello\r\n")
        val response = input.readASCIILine()
        println("Server said: '$response'")
    }
}
```

