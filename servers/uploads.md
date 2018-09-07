---
title: Uploads
caption: Handling HTTP Uploads  
category: servers
keywords: multipart receiving reading files
permalink: /servers/uploads.html
priority: 1200
---

Ktor supports handling HTTP Uploads. As well as [receiving any other kind of content](/servers/calls/requests.html).

You can check out the [Youkube example](/samples/youkube.html) for a full example of it in action.
{: .note.example }

There is a [known bug](https://github.com/ktorio/ktor/issues/364) preventing this feature from working in 0.9.1. It is already fixed in master and 0.9.2.
{: .note.bug }

## Receiving files using multipart

```kotlin
val multipart = call.receiveMultipart()
multipart.forEachPart { part ->
    when (part) {
        is PartData.FormItem -> {
            if (part.name == "title") {
                title = part.value
            }
        }
        is PartData.FileItem -> {
            val ext = File(part.originalFileName).extension
            val file = File(uploadDir, "upload-${System.currentTimeMillis()}-${session.userId.hashCode()}-${title.hashCode()}.$ext")
            part.streamProvider().use { its -> file.outputStream().buffered().use { its.copyToSuspend(it) } }
            videoFile = file
        }
    }

    part.dispose()
}


suspend fun InputStream.copyToSuspend(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    yieldSize: Int = 4 * 1024 * 1024,
    dispatcher: CoroutineDispatcher = ioCoroutineDispatcher
): Long {
    return withContext(dispatcher) {
        val buffer = ByteArray(bufferSize)
        var bytesCopied = 0L
        var bytesAfterYield = 0L
        while (true) {
            val bytes = read(buffer).takeIf { it >= 0 } ?: break
            out.write(buffer, 0, bytes)
            if (bytesAfterYield >= yieldSize) {
                yield()
                bytesAfterYield %= yieldSize
            }
            bytesCopied += bytes
            bytesAfterYield += bytes
        }
        return@withContext bytesCopied
    }
}
```
