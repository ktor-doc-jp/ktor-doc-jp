---
title: Compression
caption: Enable HTTP Compression Facilities
section: Features
permalink: /features/compression.html
feature:
    artifact: io.ktor:ktor-server-core:$ktor_version
    class: io.ktor.features.Compression
---

Compression feature adds the ability to compress outgoing content using gzip, deflate or custom encoder and thus reduce the
size of the response.

```kotlin
install(Compression)
```

{% include feature.html %}

### Configuration

When the configuration block is omitted, the default configuration is used. It includes
 the following encoders:
 
 * gzip
 * deflate
 * identity
 
If you want to select specific encoders you need to provide a configuration block:

```kotlin
install(Compression) {
    gzip()
}
```

Each encoder can be configured with a priority and some conditions: 

```kotlin
install(Compression) {
    gzip {
        priority = 1.0
    }
    deflate {
        priority = 10.0 
        minimumSize(1024) // condition
    }
}
```

Encoders are sorted by specified quality in an `Accept-Encoding` header in the HTTP request, and
then by specified priority. First encoder that satisfies all conditions wins.

In the example above when `Accept-Encoding` doesn't specify quality, `gzip` will be selected for all contents 
less than 1K in size, and all the rest will be encoded with `deflate` encoder. 

Some typical conditions are readily available:

* `minimumSize` – minimum size of the response to compress
* `matchContentType` – one or more content types that should be compressed
* `excludeContentType` – do not compress these content types

You can also use a custom condition by providing a predicate:

```kotlin
gzip {
    condition {
        parameters["e"] == "1"
    }
}
```

### Extensibility

You can provide your own encoder by implementing the `CompressionEncoder` interface and providing a configuration function. 
Since content can be provided as a `ReadChannel` or `WriteChannel`, it should be able to compress in both ways. 
See `GzipEncoder` as an example of an encoder. 
