---
title: ストリーミング
caption: ストリーミングデータを扱う
category: clients
permalink: /clients/http-client/quick-start/streaming.html
redirect_from:
- /clients/http-client/calls/streaming.html
ktor_version_review: 1.3.0
---


ほとんどのレスポンスデータはメモリ上に完全な状態で保持されます。
一方で、ストリーミングデータとしてフェッチすることもできます。

## スコープ付きストリーミング

ストリーミングを行うためには複数の方法がありますが、最も安全な方法はスコープ付きの `execute` ブロックで
[HttpStatement](https://api.ktor.io/{{ site.ktor_version }}/io.ktor.client.statement/-http-statement/) を用いる方法です。

```kotlin
client.get<HttpStatement>.execute { response: HttpResponse ->
    // レスポンスはこの時点ではダウンロードされない
    val channel = response.receive<ByteReadChannel>()
}
```

`execute` ブロックを抜けると、ネットワークリソースは開放されます。

`execute` メソッドに別の型を指定することもできます。

```kotlin
client.get<HttpStatement>.execute<ByteReadChannel> { channel: ByteReadChannel ->
    // ...
}
```