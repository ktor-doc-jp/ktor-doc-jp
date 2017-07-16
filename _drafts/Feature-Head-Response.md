---
title: Automatic HEAD Response
tags: [feature]
permalink: drafts/features/head-response.html
---


/////

THIS NEEDS TO BE RENAMED TO AUTOHEAD or FINAL NAME. See

https://github.com/Kotlin/ktor/issues/151


This feature automatically responds to `HEAD` requests by routing as if it were `GET` response and then discarding 
the body. Since any `FinalContent` produced by the system has lazy content semantics, it doesn't incur full performance
cost of processing a `GET` request with a body. 

### Usage

```kotlin
fun Application.main() {
  ...
  install(HeadRequestSupport) 
  ...
}
```

### Configuration

This feature doesn't have any configuration object, but configuration script is executed if present.
