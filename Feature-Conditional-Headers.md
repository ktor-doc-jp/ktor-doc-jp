ConditionalHeaders feature adds ability to avoid sending content if client already has same content. It does so by
checking `ETag` or `LastModified` properties of the `Resource` or `FinalContent` being sent and comparing these 
properties to what client indicates it is having. If conditions allow it entire content is not sent and 
"304 Not Modified" response is sent instead. 

```kotlin
install(ConditionalHeaders)
```

## Configuration

This feature doesn't have any configuration object, but configuration script is executed if present.

## Extensibility

`Version` interface implementations are attached to `Resource` instances, and you can return custom implementations
with your own logic. Please note that `FinalContent` is only checked for `ETag` and `LastModified` headers.
