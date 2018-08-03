---
title: Utilities
caption: Utilities exposed by ktor
category: advanced
permalink: /advanced/utilities.html
keywords: >-
    utilities formUrlEncode url-encoded application/x-www-form-urlencoded map to URL encoded string list to url encoded string Parameters parametersOf ApplicationCall.respondUrlEncoded
---

## Handling URL-encoded properties
{: #url-encoded}

Ktor exposes a few extension methods for parsing and generating url-encoded strings (`application/x-www-form-urlencoded`).

URL-encoded strings looks like this: `param=value&other=hi`.

### Parsing:
{: #url-encoded-parse}

There is an extension method for `String` that allows to get a parsed `Parameters` object from it. You can limit
the maximum number of parsed parameters with the optional `limit` parameter.

```kotlin
fun String.parseUrlEncodedParameters(defaultEncoding: Charset = Charsets.UTF_8, limit: Int = 1000): Parameters
```

### Encoding:
{: #url-encoded-encode}

Either from a List of Pairs of strings or a `Parameters` instance, you can generate an url-encoded string.

```kotlin
fun List<Pair<String, String?>>.formUrlEncode(): String
fun List<Pair<String, String?>>.formUrlEncodeTo(out: Appendable)
fun Parameters.formUrlEncode(): String
fun Parameters.formUrlEncodeTo(out: Appendable)
```

You can construct a URL-encoded string, from `List<Pair<String, String>>` like this:

```kotlin
listOf(
	"error" to "invalid_request",
	"error_description" to "client_id is missing"
).formUrlEncode()
```

You can also construct it from a `Parameters` instance, that you can instantiate by using the `Parameters.build` builder, and then calling the `formUrlEncode` extension,
or with the `parametersOf()` builder method:

```kotlin
Parameters.build {
	append("error", "invalid_request")
	append("error_description", "client_id is missing")
}.formUrlEncode()
```

The `parametersOf` builder has several signatures and there is `Parameters` operator overloading for `+` to join two `Parameters` instances:

```kotlin
parametersOf("a" to "b1", "a" to "b2").formUrlEncode()
(parametersOf("a", "b1") + parametersOf("a", "b2")).formUrlEncode()
parametersOf("a", listOf("b1", "b2")).formUrlEncode()
```

From a normal `Map<String, String>`, you can also construct a URL encoded string, but you will have first to make a list from it:

```kotlin
mapOf(
	"error" to "invalid_request",
	"error_description" to "client_id is missing"
).toList().formUrlEncode()
```

URL encoded strings allow to have repeated keys, if you use a `Map<String, String>` as base, you won't be able to
represent repeated keys.
{: .note}

You can also construct it from a `Map<String, List<String>>` by *flatMapping* it first:

```kotlin
mapOf(
    "error" to listOf("invalid_request"),
    "error_descriptions" to listOf("client_id is missing", "server error")
).flatMap { map -> map.value.map { map.key to it } }.formUrlEncode()
```

In the case you want to avoid constructing a whole String with the content,
and want to write to somewhere directly, you can use the `formUrlEncodeTo` methods instead.

#### Responding URL-encoded

You can do something like:

```kotlin
call.respondUrlEncoded("hello" to listOf("world"))
```

by adding the following extension methods to your project:

```kotlin
suspend fun ApplicationCall.respondUrlEncoded(vararg keys: Pair<String, List<String>>) =
    respondUrlEncoded(parametersOf(*keys))

suspend fun ApplicationCall.respondUrlEncoded(parameters: Parameters) =
    respondWrite(ContentType.Application.FormUrlEncoded) {
        parameters.formUrlEncodeTo(this)
    }
```
