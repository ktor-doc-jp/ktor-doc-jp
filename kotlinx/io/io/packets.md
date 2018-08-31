---
title: Packets
caption: Packets
category: kotlinx
toc: true
priority: 2
---

Packets are small chunks of data representing messages, packets or chunks of information.
They are built and consumed synchronously.

## Building packets

```kotlin
fun buildPacket(headerSizeHint: Int = 0, block: BytePacketBuilder.() -> Unit): ByteReadPacket
fun BytePacketBuilder(headerSizeHint: Int = 0): BytePacketBuilder

class BytePacketBuilder(headerSizeHint: Int, pool: ObjectPool<IoBuffer>) {
    val size: Int
    val isEmpty: Boolean
    val isNotEmpty: Boolean

    override fun append(c: Char): BytePacketBuilder
    override fun append(csq: CharSequence?): BytePacketBuilder
    override fun append(csq: CharSequence?, start: Int, end: Int): BytePacketBuilder
    override fun release()
    override fun flush()
    override fun close()
    fun <R> preview(block: (tmp: ByteReadPacket) -> R): R
    fun build(): ByteReadPacket
    override fun writePacket(p: ByteReadPacket)
    override fun last(buffer: IoBuffer)
    final override var byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN
    internal var tail: IoBuffer = IoBuffer.Empty
    final override fun writeFully(src: ByteArray, offset: Int, length: Int)
    final override fun writeLong(v: Long)
    final override fun writeInt(v: Int)
    final override fun writeShort(v: Short)
    final override fun writeByte(v: Byte)
    final override fun writeDouble(v: Double)
    final override fun writeFloat(v: Float)
    override fun writeFully(src: ShortArray, offset: Int, length: Int)
    override fun writeFully(src: IntArray, offset: Int, length: Int)
    override fun writeFully(src: LongArray, offset: Int, length: Int)
    override fun writeFully(src: FloatArray, offset: Int, length: Int)
    override fun writeFully(src: DoubleArray, offset: Int, length: Int)
    override fun writeFully(src: IoBuffer, length: Int)
    override fun fill(n: Long, v: Byte)
    override fun append(c: Char): BytePacketBuilderBase
    override fun append(csq: CharSequence?): BytePacketBuilderBase
    override fun append(csq: CharSequence?, start: Int, end: Int): BytePacketBuilderBase
    open fun writePacket(p: ByteReadPacket)
    fun writePacket(p: ByteReadPacket, n: Int)
    fun writePacket(p: ByteReadPacket, n: Long)
    override fun append(csq: CharArray, start: Int, end: Int): Appendable
    fun writeStringUtf8(s: String)
    fun writeStringUtf8(cs: CharSequence)
    fun release()
    override fun `$prepareWrite$`(n: Int): IoBuffer
    override fun `$afterWrite$`()
    fun reset()
}

val PACKET_MAX_COPY_SIZE: Int
```

## Reading packets