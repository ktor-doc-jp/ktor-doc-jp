---
title: Utilities
caption: Utilities exposed by ktor
category: advanced
permalink: /advanced/utilities.html
keywords: >-
    utilities formUrlEncode url-encoded
---

### Handling URL-encoded properties
{: #url-encoded}

Ktor exposes a few extension methods for parsing and generating url-encoded strings.

URL-encoded strings looks like this: `param=value&other=hi`.

#### Parsing:
{: #url-encoded-parse}

There is an extension method for `String` that allows to get a parsed `Parameters` object from it. You can limit
the maximum number of parsed parameters with the optional `limit` parameter.

```kotlin
fun String.parseUrlEncodedParameters(defaultEncoding: Charset = Charsets.UTF_8, limit: Int = 1000): Parameters
```

#### Encoding:
{: #url-encoded-encode}

Either from a List of Pairs of strings or a `Parameters` instance, you can generate an url-encoded string.

```kotlin
fun List<Pair<String, String?>>.formUrlEncode(): String
fun List<Pair<String, String?>>.formUrlEncodeTo(out: Appendable)
fun Parameters.formUrlEncode(): String
fun Parameters.formUrlEncodeTo(out: Appendable)
```
