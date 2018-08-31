---
title: IoBuffer
caption: IoBuffer
category: kotlinx
toc: true
priority: 0
---

Fixed size buffers in memory implementing the Input and Output interfaces.

### IOBuffer

```kotlin
class IoBuffer : Input, Output {
    internal val origin: IoBuffer?
    var next: IoBuffer?
    var attachment: Any?
    val capacity: Int
    val startGap: Int
    val endGap: Int
    fun canRead(): Boolean
    fun canWrite(): Boolean
    val readRemaining: Int
    val writeRemaining: Int
    fun reserveStartGap(n: Int)
    fun reserveEndGap(n: Int)

    var byteOrder: ByteOrder

    fun readByte(): Byte
    fun readShort(): Short
    fun readInt(): Int
    fun readLong(): Long
    fun readFloat(): Float
    fun readDouble(): Double

    fun readFully(dst: ByteArray, offset: Int, length: Int)
    fun readFully(dst: ShortArray, offset: Int, length: Int)
    fun readFully(dst: IntArray, offset: Int, length: Int)
    fun readFully(dst: LongArray, offset: Int, length: Int)
    fun readFully(dst: FloatArray, offset: Int, length: Int)
    fun readFully(dst: DoubleArray, offset: Int, length: Int)
    fun readFully(dst: IoBuffer, length: Int)

    fun readAvailable(dst: ByteArray, offset: Int, length: Int): Int
    fun readAvailable(dst: ShortArray, offset: Int, length: Int): Int
    fun readAvailable(dst: IntArray, offset: Int, length: Int): Int
    fun readAvailable(dst: LongArray, offset: Int, length: Int): Int
    fun readAvailable(dst: FloatArray, offset: Int, length: Int): Int
    fun readAvailable(dst: DoubleArray, offset: Int, length: Int): Int
    fun readAvailable(dst: IoBuffer, length: Int): Int

    fun tryPeek(): Int

    fun discard(n: Long): Long

    fun discardExact(n: Int)

    fun writeByte(v: Byte)
    fun writeShort(v: Short)
    fun writeInt(v: Int)
    fun writeLong(v: Long)
    fun writeFloat(v: Float)
    fun writeDouble(v: Double)

    fun writeFully(src: ByteArray, offset: Int, length: Int)
    fun writeFully(src: ShortArray, offset: Int, length: Int)
    fun writeFully(src: IntArray, offset: Int, length: Int)
    fun writeFully(src: LongArray, offset: Int, length: Int)
    fun writeFully(src: FloatArray, offset: Int, length: Int)
    fun writeFully(src: DoubleArray, offset: Int, length: Int)
    fun writeFully(src: IoBuffer, length: Int)

    fun appendChars(csq: CharArray, start: Int, end: Int): Int
    fun appendChars(csq: CharSequence, start: Int, end: Int): Int

    fun append(c: Char): Appendable
    fun append(csq: CharSequence?): Appendable
    fun append(csq: CharSequence?, start: Int, end: Int): Appendable
    fun append(csq: CharArray, start: Int, end: Int): Appendable

    fun fill(n: Long, v: Byte)

    fun close()
    fun write(array: ByteArray, offset: Int, length: Int)
    fun writeBuffer(src: IoBuffer, length: Int): Int
    fun pushBack(n: Int)
    fun resetForWrite()
    fun resetForWrite(limit: Int)
    fun resetForRead()
    fun isExclusivelyOwned(): Boolean
    fun makeView(): IoBuffer
    fun release(pool: ObjectPool<IoBuffer>)

    fun flush()

    companion object {
        val Empty: IoBuffer
        val Pool: ObjectPool<IoBuffer>
        val NoPool: ObjectPool<IoBuffer>
        val EmptyPool: ObjectPool<IoBuffer>
    }
}
```

### Closeable

```kotlin
interface Closeable {
    fun close()
}

inline fun <C : Closeable, R> C.use(block: (C) -> R): R
```

### Input 

```kotlin
interface Input : Closeable {
    var byteOrder: ByteOrder
    val endOfInput: Boolean

    fun readByte(): Byte
    fun readShort(): Short
    fun readInt(): Int
    fun readLong(): Long
    fun readFloat(): Float
    fun readDouble(): Double

    fun readFully(dst: ByteArray, offset: Int, length: Int)
    fun readFully(dst: ShortArray, offset: Int, length: Int)
    fun readFully(dst: IntArray, offset: Int, length: Int)
    fun readFully(dst: LongArray, offset: Int, length: Int)
    fun readFully(dst: FloatArray, offset: Int, length: Int)
    fun readFully(dst: DoubleArray, offset: Int, length: Int)
    fun readFully(dst: IoBuffer, length: Int = dst.writeRemaining)

    fun readAvailable(dst: ByteArray, offset: Int, length: Int): Int
    fun readAvailable(dst: ShortArray, offset: Int, length: Int): Int
    fun readAvailable(dst: IntArray, offset: Int, length: Int): Int
    fun readAvailable(dst: LongArray, offset: Int, length: Int): Int
    fun readAvailable(dst: FloatArray, offset: Int, length: Int): Int
    fun readAvailable(dst: DoubleArray, offset: Int, length: Int): Int
    fun readAvailable(dst: IoBuffer, length: Int): Int

    fun tryPeek(): Int
    fun discard(n: Long): Long

    fun close()
}


fun Input.readFully(dst: ByteArray, offset: Int = 0, length: Int = dst.size)
fun Input.readFully(dst: ShortArray, offset: Int = 0, length: Int = dst.size)
fun Input.readFully(dst: IntArray, offset: Int = 0, length: Int = dst.size)
fun Input.readFully(dst: LongArray, offset: Int = 0, length: Int = dst.size)
fun Input.readFully(dst: FloatArray, offset: Int = 0, length: Int = dst.size)
fun Input.readFully(dst: DoubleArray, offset: Int = 0, length: Int = dst.size)
fun Input.readFully(dst: IoBuffer, length: Int = dst.writeRemaining)
fun Input.readAvailable(dst: ByteArray, offset: Int = 0, length: Int = dst.size): Int
fun Input.readAvailable(dst: ShortArray, offset: Int = 0, length: Int = dst.size): Int
fun Input.readAvailable(dst: IntArray, offset: Int = 0, length: Int = dst.size): Int
fun Input.readAvailable(dst: LongArray, offset: Int = 0, length: Int = dst.size): Int
fun Input.readAvailable(dst: FloatArray, offset: Int = 0, length: Int = dst.size): Int
fun Input.readAvailable(dst: DoubleArray, offset: Int = 0, length: Int = dst.size): Int
fun Input.readAvailable(dst: IoBuffer, length: Int = dst.writeRemaining): Int
fun Input.discard(): Long
fun Input.discardExact(n: Long)
fun Input.discardExact(n: Int)

inline fun Input.takeWhile(block: (IoBuffer) -> Boolean)
inline fun Input.takeWhileSize(initialSize: Int = 1, block: (IoBuffer) -> Int)
```

### Output 

```kotlin
interface Output : Appendable, Closeable {
    var byteOrder: ByteOrder

    fun writeByte(v: Byte)
    fun writeShort(v: Short)
    fun writeInt(v: Int)
    fun writeLong(v: Long)
    fun writeFloat(v: Float)
    fun writeDouble(v: Double)

    fun writeFully(src: ByteArray, offset: Int, length: Int)
    fun writeFully(src: ShortArray, offset: Int, length: Int)
    fun writeFully(src: IntArray, offset: Int, length: Int)
    fun writeFully(src: LongArray, offset: Int, length: Int)
    fun writeFully(src: FloatArray, offset: Int, length: Int)
    fun writeFully(src: DoubleArray, offset: Int, length: Int)
    fun writeFully(src: IoBuffer, length: Int)

    fun append(csq: CharArray, start: Int, end: Int): Appendable

    fun fill(n: Long, v: Byte)

    fun flush()
    fun close()
}

fun Output.append(csq: CharSequence, start: Int = 0, end: Int = csq.length): Appendable
fun Output.append(csq: CharArray, start: Int = 0, end: Int = csq.size): Appendable
fun Output.writeFully(src: ByteArray, offset: Int = 0, length: Int = src.size)
fun Output.writeFully(src: ShortArray, offset: Int = 0, length: Int = src.size)
fun Output.writeFully(src: IntArray, offset: Int = 0, length: Int = src.size)
fun Output.writeFully(src: LongArray, offset: Int = 0, length: Int = src.size)
fun Output.writeFully(src: FloatArray, offset: Int = 0, length: Int = src.size)
fun Output.writeFully(src: DoubleArray, offset: Int = 0, length: Int = src.size)
fun Output.writeFully(src: IoBuffer, length: Int = src.readRemaining)
fun Output.fill(n: Long, v: Byte = 0)
inline fun Output.writeWhile(block: (IoBuffer) -> Boolean)
inline fun Output.writeWhileSize(initialSize: Int = 1, block: (IoBuffer) -> Int)
fun Output.writePacket(packet: ByteReadPacket)
```