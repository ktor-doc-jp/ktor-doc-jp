---
title: Channels
caption: Channels
category: kotlinx
toc: true
priority: 1
---

ByteChannels are asynchronous streams, potentially large without seeking capabilities.
They are useful for reading and writing to sockets, or files from the start to the end.
Usually you read and create chunks of synchronous data called [Packets](/kotlinx/io/packets.html) from it,
reducing the amount of suspensions to packets.

## ByteReadChannel

```kotlin
interface ByteReadChannel {
    val availableForRead: Int
    val isClosedForRead: Boolean
    val isClosedForWrite: Boolean
    var readByteOrder: ByteOrder
    suspend fun readAvailable(dst: ByteArray, offset: Int, length: Int): Int
    suspend fun readAvailable(dst: IoBuffer): Int
    suspend fun readAvailable(dst: ArrayBuffer, offset: Int, length: Int): Int

    suspend fun readFully(dst: ByteArray, offset: Int, length: Int)
    suspend fun readFully(dst: IoBuffer, n: Int)
    suspend fun readFully(dst: ArrayBuffer, offset: Int, length: Int)

    suspend fun readPacket(size: Int, headerSizeHint: Int): ByteReadPacket
    suspend fun readRemaining(limit: Long, headerSizeHint: Int): ByteReadPacket
    suspend fun readLong(): Long
    suspend fun readInt(): Int
    suspend fun readShort(): Short
    suspend fun readByte(): Byte
    suspend fun readBoolean(): Boolean
    suspend fun readDouble(): Double
    suspend fun readFloat(): Float
    fun readSession(consumer: ReadSession.() -> Unit)
    suspend fun readSuspendableSession(consumer: suspend SuspendableReadSession.() -> Unit)
    suspend fun <A : Appendable> readUTF8LineTo(out: A, limit: Int): Boolean
    suspend fun readUTF8Line(limit: Int): String?
    fun cancel(cause: Throwable?): Boolean
    suspend fun discard(max: Long): Long

    companion object {
        val Empty: ByteReadChannel
    }
}
```

## ByteWriteChannel

WIP
