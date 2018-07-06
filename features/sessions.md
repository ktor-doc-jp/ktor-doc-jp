---
title: Sessions
caption: Handle Conversations with Sessions
category: features
permalink: /features/sessions.html
keywords: custom session serializers, custom session transformers, custom session storage providers
feature:
    artifact: io.ktor:ktor-server-core:$ktor_version
    artifact2: io.ktor:ktor-server-sessions:$ktor_version
    class: io.ktor.sessions.Sessions
---

{::options toc_levels="1..3" /}

A session is a mechanism to persist data between different HTTP requests. Establishing a conversational
context into the otherwise stateless nature of HTTP. They allow servers to keep a piece of information associated
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

{% include feature.html %}

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

To get the session content, you have to call the `call.sessions.get` method receiving as type parameter one
of the registered session types:

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

When a user logs out, or a session should be cleared for any other reason, you can call the `clear` function:

```kotlin
call.sessions.clear<MySession>()
```

## Multiple sessions
{: #multiple-sessions}

Since there could be several conversational states for a single application, you can install multiple session mapping:

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

But you will want to create sessions and then configure them. You can configure the sessions in several different ways:

* *Where is the payload stored:* client-side, or server-side.
* *How is the payload or the session id transferred:* Using cookies or headers.
* *How are they serialized:* Using an internal format, JSON, a custom engine...
* *Where is the payload stored in the server:* Using memory, a folder, redis...
* *Payload transformations:* Encrypted, authenticated...

Since sessions can be implemented by various techniques, there is an extensive configuration facility to set them up:

* `cookie` will install cookie-based transport
* `header` will install header-based transport 

Each of these functions will get the name of the cookie or header. 

If a function is passed an argument of type `SessionStorage` it will use the storage to save the session, otherwise
it will serialize the data into the cookie/header value.

Each of these functions can receive an optional configuration lambda.

For cookies, the receiver is `CookieSessionBuilder` which allows you to:

* specify custom `serializer`
* add a value `transformer`, like signing or encrypting
* specify the cookie configuration such as duration, encoding, domain, path and so on

For headers, the receiver is `HeaderSessionBuilder` which allows `serializer` and `transformer` customization.

For cookies & headers that are server-side with a `SessionStorage`, additional configuration is `identity` function
that should generate a new ID when the new session is created.

### Cookies vs Headers sessions
{: #cookies-headers }

Depending on the consumer, you might want to transfer the sessionId or the payload using a cookie,
or a header. For example, for a website, you will normally use cookies, while for an API you might want to use headers.

The Sessions.Configuration provide two methods `cookie` and `header` to select how to transfer the sessions: 

#### Cookies

```kotlin
application.install(Sessions) {
    cookie<MySession>("SESSION")
} 
```

You can configure the cookie by providing an additional block. There is a cookie property allowing
to configure it, for example by adding a [SameSite extension](https://caniuse.com/#search=samesite):

```kotlin
application.install(Sessions) {
    cookie<MySession>("SESSION") {
        cookie.extensions["SameSite"] = "lax"
    }
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

#### Client-side sessions and transforms
{: #client}

Without additional arguments for the `cookie` and `header` methods, the session is configured to keep the payload
at the client. And the full payload will be sent back and forth. In this mode, you can, and should apply
transforms to encrypt or authenticate sessions:

```kotlin
application.install(Sessions) {
    cookie<MySession>("SESSION") {
        val secretSignKey = hex("000102030405060708090a0b0c0d0e0f")
        transform(SessionTransportTransformerMessageAuthentication(secretSignKey))
    }
} 
```

You should only use client-side sessions if your payload can't suffer from replay attacks. Also if you need to prevent
modifications, ensure that you are transforming the session with at least authentication, but ideally with encryption too.
This should prevent payload modification if you keep your secret key safe. But remember that if your key is compromised
and you have to change the key, all the sessions will effectively be invalid.
{: .note.security }

##### SessionTransportTransformerDigest
{: #SessionTransportTransformerDigest}

The `SessionTransportTransformerEncrypt` provides a session transport transformer that includes
a hash of the payload with a salt and verifies it. It uses `SHA-256` as the default
hashing algorithm, but it can be changed. It doesn't encrypt the payload, but still without the salt people
shouldn't be able to change it.

```kotlin
// REMEMBER! Change this string and store them safely
val salt = "my unity salt string"
cookie<TestUserSession>(cookieName) {
    transform(SessionTransportTransformerDigest(salt))
}
``` 

##### SessionTransportTransformerMessageAuthentication
{: #SessionTransportTransformerMessageAuthentication}

The `SessionTransportTransformerEncrypt` provides a session transport transformer that includes
an authenticated hash of the payload and verifies it. It is similar to SessionTransportTransformerDigest
but uses a HMAC. It uses `HmacSHA1` as the default authentication algorithm, but it can be changed.
It doesn't encrypt the payload, but still without the key people shouldn't be able to change it.

```kotlin
// REMEMBER! Change this string and store them safely
val key = hex("03515606058610610561058")
cookie<TestUserSession>(cookieName) {
    transform(SessionTransportTransformerMessageAuthentication(key))
}
``` 

##### SessionTransportTransformerEncrypt
{: #SessionTransportTransformerEncrypt}

The `SessionTransportTransformerEncrypt` provides a session transport transformer that encrypts the payload
and authenticates it. By default it uses `AES` and `HmacSHA256`, but you can configure it. It requires 
an encryption key and an authentication key compatible in size with the algorithms: 

```kotlin
// REMEMBER! Change ALL the digits in those hex numbers and store them safely
val secretEncryptKey = hex("00112233445566778899aabbccddeeff") 
val secretAuthKey = hex("02030405060708090a0b0c")
cookie<TestUserSession>(cookieName) {
    transform(SessionTransportTransformerEncrypt(secretEncryptKey, secretAuthKey))
}
``` 

#### Server-side sessions and storages
{: #server}

If you specify storage, then the session will be configured to be stored on the server using that storage, and
a sessionId will be transferred between the server and the client instead of the full payload: 

```kotlin
application.install(Sessions) {
    cookie<MySession>("SESSION", storage = SessionStorageMemory())
} 
```

There are two predefined storages: `SessionStorageMemory`, `DirectoryStorage`. And another composable storage: `CacheStorage`.

`DirectoryStorage` and `CacheStorage` are dependant on the `io.ktor:ktor-server-sessions:$ktor_version` artifact.
{: .note.artifact } 

### Serializers
{: #serializer}

You can specify a custom serializer with:

```kotlin
application.install(Sessions) {
    cookie<MySession>("SESSION") {
        serializer = MyCustomSerializer()
    }
} 
```

If you do not specify any serializer, it will use one with an internal optimized format.

#### SessionSerializerReflection
{: #SessionSerializerReflection}

This is the default serializer, when no serializer is specified:

```kotlin
cookie<MySession>("SESSION") {
    serializer = autoSerializerOf()
}
```

#### GsonSessionSerializer
{: #GsonSessionSerializer}

Using JSON instead of the default serializer. Note that the payload will be bigger:

```kotlin
cookie<MySession>("SESSION") {
    serializer = gsonSessionSerializer()
}
```

This serializes requires the artifact `io.ktor:ktor-gson:$ktor_version`.
{: .note}

## Extending
{: #extending}

Sessions are designed to be extensible, allowing you to provide: custom serializers, transformers, and storage providers.

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

You can use these transformations to encrypt, authenticate, or transform the Payload.
You have to implement that interface and add the transformer as usual:

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

In your implementations, you have to call the callbacks providing a ByteWriteChannel and a ByteReadChannel
that you have to provide: it is your responsibility to open and close them.
You can read more about `ByteWriteChannel` and `ByteReadChannel` in their libraries documentation.
If you just need to load or store a ByteArray, you can use this snippet which provides a simplified session storage:

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

## Invalidating Client-side sessions
{: #invalidating-client-sessions }

Since client-side sessions can't be invalidated directly like server sessions. You can manually mark an expiration
time for the session by including an expiration timestamp as part of your session payload.

For example:

```kotlin
data class MyExpirableSession(val name: String, val expiration: Long)

fun Application.main() {
    routing {
        get("/user/panel") {
            val session = call.getMyExpirableSession()
            call.respondText("Welcome ${session.name}")
        }
    }
}

fun ApplicationCall.getMyExpirableSession(): MyExpirableSession {
    val session = sessions.get<MyExpirableSession>() ?: error("No session found")
    if (System.currentTimeMillis() > session.expiration) {
        error("Session expired")
    }
    return session
}
```