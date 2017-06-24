Compression feature adds ability to compress outgoing content using gzip, deflate or custom encoder and thus reduce
size of the response.

```kotlin
install(Compression)
```

## Configuration

When configuration block is omitted, the default configuration is used. It includes
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

Each encoder can be configured with a priority and a number of conditions: 
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

A number of typical conditions are readily available:
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

## Extensibility

You can provide your own encoder by implementing `CompressionEncoder` interface and providing configuration function. 
Since content can be provided as a `ReadChannel` or `WriteChannel` it should be able to compress in both ways. 
See `GzipEncoder` as an example of an encoder. 
