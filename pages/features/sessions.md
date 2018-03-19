---
title: Sessions
caption: Handle Conversations with Sessions
section: Features
permalink: /features/sessions.html
keywords: custom session serializers, custom session transformers, custom session storage providers
feature:
    artifact: io.ktor:ktor-server-core:$ktor_version
    class: io.ktor.sessions.Sessions
---

{::options toc_levels="1..3" /}

A session is a mechanism to persist data between different HTTP requests. Establishing a conversational
context into otherwise stateless HTTP nature. They allow servers to keep a piece of information associated
with the client during a sequence of HTTP requests and responses.
 
Different use-cases include: authentication and authorization, user tracking, keeping information
at client like a shopping cart,  and more.

Sessions are typically implemented by employing `Cookies`, but could also be done using headers for example
to be consumed by other backends or AJAX requests.

They are either client-side when the entire serialized object goes back and forth between the client and the server,
or server-side when only the session ID is transferred and the associated data is stored entirely in the server. 

**Table of contents:**

* TOC
{:toc}

{% include feature/feature.html %}

## Basic installation
{: #install-basic}


## Installation
{: #install-advanced}

The basic installation of the Sessions feature looks like this.

```kotlin
application.install(Sessions) {
    cookie<MySession>("SESSION")
} 
```

## Usage
{: #usage }

In order to access or set the session content, you use the `call.sessions` property:

To get the session content, you have to call `call.sessions.get` method reciving as type parameter one
of the registered sesion types:

```kotlin
application.routing {
    get("/") {
        val mySession: MySession = call.sessions.get<MySession>()
    }
}
```

If the session was not set, the returned value will be null.
{: .note}

To create or modify current session you just call a `set` function on a `sessions` property with the value of the
data class: 

```kotlin
call.sessions.set(MySession(name = "John", value = 12))
```

When user logs out or session should be cleared for some other reason, you call `clear` function:

```kotlin
call.sessions.clear<MySession>()
```

## Multiple sessions
{: #multiple-sessions}

Since there could be several conversational states for a single application, you can install multiple session mappings:

```kotlin
application.install(Sessions) {
    cookie<Session1>("Session1") // install a cookie stateless session
    header<Session2>("Session2", sessionStorage) { // install a header server-side session
        transform(SessionTransportTransformerDigest()) // sign the ID that travels to client
    }
}
``` 

For multiple session mapping, both type and name should be unique. 

## Configuration
{: #configuration}

But you will want to create sessions and to configure them. You can configure sessions in several ways:

* *Where is the payload stored:* client-side, or server-side.
* *How is the payload or the session id transferred:* Using cookies or headers.
* *How are they serialized:* Using an internal format, JSON, a custom engine...
* *Where is the payload stored in the server:* Using memory, a folder, redis...
* *Payload transformations:* Encrypted, authenticated...

Since sessions can be implemented by various techniques, there is an extensive configuration facility to set them up:

* `cookie` will install cookie-based transport
* `header` will install header-based transport 

Each of these functions will get the name of the cookie or header. 

If the function is passed an argument of type `SessionStorage` it will use the storage to save the session, otherwise
it will serialize data into the cookie/header value.

Each of these functions can receive an optional configuration lambda.

For cookies, the receiver is `CookieSessionBuilder` which allows to:

* specify custom `serializer`
* add a value `transformer`, like signing or encrypting
* specify cookie configuration such as duration, encoding, domain, path and so on

For headers, the receiver is `HeaderSessionBuilder` that allows `serializer` and `transformer` customization.

For cookies & headers that are server-side with a `SessionStorage`, additional configuration is `identity` function
that should generate a new ID when the new session is created.

### Cookies vs Headers sessions
{: #cookies-headers }

Depending on the consumer, you might want to transfer the sessionId or the payload using a cookie,
or a hader. For example, for a website you will normally use cookies, while for an API you might want to use headers.

The Sessions.Configuration provide two methods `cookie` and `header` to select how to transfer the sessions: 

#### Cookies

```kotlin
application.install(Sessions) {
    cookie<MySession>("SESSION")
} 
```

#### Headers

```kotlin
application.install(Sessions) {
    header<MySession>("SESSION")
} 
```

### Client-side/Server-side sessions
{: #client-server }

Depending on the application, the size of the payload and the security, you might want to put the payload of
the session in the client or the server.

#### Client-side sessions

Without additional arguments for the `cookie` and `header` methods, the session is configured to have the payload
at the client. And the full payload will be sent back and forth. In this mode, you can apply transforms  

```kotlin
application.install(Sessions) {
    cookie<MySession>("SESSION")
} 
```

This installs the `Sessions` feature and maps cookie with the name `"SESSION"` to `MySession` type in a stateless way. 
The entire class `MySession` will be serialized As a string and sent to the client as a cookie.
When the client makes another request, the cookie is deserialized back into `MySession` and it is available to the
server code:

#### Server-side sessions

```kotlin
application.install(Sessions) {
    cookie<MySession>("SESSION")
} 
```

## Extending
{: #extending}

Sessions are designed to be extensible, allowing to provide: custom serializers, transformers and storage providers.

### Custom serializers
{: #extending-serializers}

The Sessions API provides a `SessionSerializer` interface, that looks like this:

```kotlin
interface SessionSerializer {
    fun serialize(session: Any): String
    fun deserialize(text: String): Any
}
```

This interface is for a generic serializer, and you can install it like this:

```kotlin
cookie<MySession>("NAME") {
    serializer = MyCustomSerializer()
}
```

So for example you can create a JSON session serializer using Gson:

```kotlin
class GsonSessionSerializer(val type: java.lang.reflect.Type, val gson: Gson = Gson(), configure: Gson.() -> Unit = {}) : SessionSerializer {
    init {
        configure(gson)
    }

    override fun serialize(session: Any): String = gson.toJson(session)
    override fun deserialize(text: String): Any = gson.fromJson(text, type)
}
```

And configuring it:

```kotlin
cookie<MySession>("NAME") {
    serializer = GsonSessionSerializer(MySession::class.java)
}
```

### Custom transform transformers
{: #extending-transform-transformers}

The Sessions API provides a `SessionTransportTransformer` interface, that looks like this:

```kotlin
interface SessionTransportTransformer {
    fun transformRead(transportValue: String): String?
    fun transformWrite(transportValue: String): String
}
```

You can use these transformations to encrypt, authenticate, or generally transform the Payload.
You have to implement that interface and add the transfomer as usual:

```kotlin
cookie<MySession>("NAME") {
    transform(MtSessionTransformer)
}
```

### Custom storages
{: #extending-storages}

The Sessions API provides a `SessionStorage` interface, that looks like this:

```kotlin
interface SessionStorage {
    suspend fun write(id: String, provider: suspend (ByteWriteChannel) -> Unit)
    suspend fun invalidate(id: String)
    suspend fun <R> read(id: String, consumer: suspend (ByteReadChannel) -> R): R
}
```

All three functions are marked as `suspend` and are designed to be fully asynchronous
and use `ByteWriteChannel` and `ByteReadChannel` from `kotlinx.coroutines.io` that provide
APIs for reading and writing from an asynchronous Channel.

In your implementations you have to call the callbacks providing a ByteWriteChannel and a ByteReadChannel
that you have to provide: it is your responsibility to open and close them.
You can read more about `ByteWriteChannel` and `ByteReadChannel` in their libraries documentation.
If you just need to load or store a ByteArray, you can use this snipped that provides a simplified session storage:

```kotlin
abstract class SimplifiedSessionStorage : SessionStorage {
    abstract suspend fun read(id: String): ByteArray?
    abstract suspend fun write(id: String, data: ByteArray?): Unit

    override suspend fun invalidate(id: String) {
        write(id, null)
    }

    override suspend fun <R> read(id: String, consumer: suspend (ByteReadChannel) -> R): R {
        val data = read(id) ?: throw NoSuchElementException("Session $id not found")
        return consumer(ByteReadChannel(data))
    }

    override suspend fun write(id: String, provider: suspend (ByteWriteChannel) -> Unit) {
        return provider(reader(coroutineContext, autoFlush = true) {
            write(id, channel.readAvailable())
        }.channel)
    }
}

suspend fun ByteReadChannel.readAvailable(): ByteArray {
    val data = ByteArrayOutputStream()
    val temp = ByteArray(1024)
    while (!isClosedForRead) {
        val read = readAvailable(temp)
        if (read <= 0) break
        data.write(temp, 0, read)
    }
    return data.toByteArray()
}
```
{: .compact}

With this simplified storage you only have to implement two simpler methods:

```kotlin
abstract class SimplifiedSessionStorage : SessionStorage {
    abstract suspend fun read(id: String): ByteArray?
    abstract suspend fun write(id: String, data: ByteArray?): Unit
}
```

So for example, a redis session storage would look like this:

```kotlin
class RedisSessionStorage(val redis: Redis, val prefix: String = "session_", val ttlSeconds: Int = 3600) :
    SimplifiedSessionStorage() {
    private fun buildKey(id: String) = "$prefix$id"

    override suspend fun read(id: String): ByteArray? {
        val key = buildKey(id)
        return redis.get(key)?.unhex?.apply {
            redis.expire(key, ttlSeconds) // refresh
        }
    }

    override suspend fun write(id: String, data: ByteArray?) {
        val key = buildKey(id)
        if (data == null) {
            redis.del(buildKey(id))
        } else {
            redis.set(key, data.hex)
            redis.expire(key, ttlSeconds)
        }
    }
}
```
