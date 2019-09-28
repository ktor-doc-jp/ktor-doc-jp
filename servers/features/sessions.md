---
title: セッション
caption: セッションによるやりとりを扱う
category: servers
permalink: /servers/features/sessions.html
children: /servers/features/sessions/
keywords: custom session serializers, custom session transformers, custom session storage providers
feature:
  artifact: io.ktor:ktor-server-core:$ktor_version
  artifact2: io.ktor:ktor-server-sessions:$ktor_version
  class: io.ktor.sessions.Sessions
redirect_from:
- /features/sessions.html
ktor_version_review: 1.0.0
---

{::options toc_levels="1..3" /}

セッションは異なるHTTPリクエスト間でデータを永続化するメカニズムです。
ステートレスな性質を持つHTTPに対し、対話的なコンテキストを確立します。
一連のHTTPリクエスト・レスポンスの間、クライアントに紐づく情報をサーバに保持させることができます。

異なるユースケースが含まれています: 認証・認可、ユーザトラッキング、ショッピングカートのようにクライアントによる情報保持など。 

セッションは典型的な方法としては`Cookies`を使って実装されていますが、
ヘッダーを使うことでも実現できます。
ヘッダーは例えば他のバックエンドやAJAXリクエストから使われたりします。

クライアントとサーバの間でシリアライズされたオブジェクト全体がやりとりされるクライアントサイドセッションと、
SessionIDだけやりとりされるがそれに紐づくデータはすべてサーバ側に保存されているサーバサイドセッションがあります。

**目次:**

* TOC
{:toc}

{% include feature.html %}

## インストール
{: #installation }

セッションは通常不変なdata classによって表現されます（そしてセッションは`.copy`メソッドを呼び出すことで変更されます）:

```kotlin
data class SampleSession(val name: String, val value: Int)
```

単純なSession機能のインストールコードは以下のようになります:

```kotlin
install(Sessions) {
    cookie<SampleSession>("COOKIE_NAME")
}
```

より発展的なインストール方法は以下のようになります:

```kotlin
install(Sessions) {
    cookie<SampleSession>(
        "SESSION_FEATURE_SESSION_ID",
        directorySessionStorage(File(".sessions"), cached = true)
    ) {
        cookie.path = "/" // Specify cookie's path '/' so it can be used in the whole site
    }
}
```

セッションを設定するため、[cookie/header](/servers/features/sessions/cookie-header.html)名を指定する必要があります。
また、[server-side storage](/servers/features/sessions/client-server.html)を任意で指定する必要もあり、セッションに紐づくクラスも指定する必要があります。

よりセッションをカスタマイズしたい場合は、[拡張](#extending)セクションを読んでください。

いくつかのセッションの設定方法の組み合わせがあり、[セッションの設定方法の決定](#configuring)セクションに記載されています。
{: .note}

## 使い方
{: #usage }

セッションコンテンツにアクセスまたは設定するために、`call.sessions`プロパティを使う必要があります:

セッションコンテンツを取得するためには、`call.sessions.get`メソッドをセッション型として登録した型パラメータとともに呼び出す必要があります。

```kotlin
routing {
    get("/") {
        // If the session was not set, or is invalid, the returned value is null.
        val mySession: MySession? = call.sessions.get<MySession>()
    }
}
```

現在のセッションを作成または変更するためには、`sessions`プロパティの`set`メソッドをdata class値を渡して単に呼び出すだけです:

```kotlin
call.sessions.set(MySession(name = "John", value = 12))
```

セッションを変更するためには（例えばカウンターをインクリメント）、`data class`の`.copy`メソッドを呼び出します:

```kotlin
val session = call.sessions.get<MySession>() ?: MySession(name = "Initial", value = 0)  
call.sessions.set(session.copy(value = session.value + 1))
```

ユーザがログアウトするか、その他の理由でセッションをクリアする場合、`clear`関数を呼び出すことができます。
When a user logs out, or a session should be cleared for any other reason, you can call the `clear` function:

```kotlin
call.sessions.clear<MySession>()
```

これを呼び出した後、再度セッションの設定を行うまではセッションへの参照はnullを返すようになります。

<div markdown='1'>
リクエストをハンドリングするときに、セッションを取得、設定、クリアできます:

```kotlin
val session = call.sessions.get<SampleSession>() // Gets a session of this type or null if not available
call.sessions.set(SampleSession(name = "John", value = 12)) // Sets a session of this type
call.sessions.clear<SampleSession>() // Clears the session of this type 
```
</div>
{: .note.summarizing }

## 複数セッション
{: #multiple-sessions}

１つのアプリケーションに対しいくつかの対話的な状態がありえるため、複数のセッションへのマッピングをインストールすることができます。
例えば:

* クライアントサイドCookieとして、ユーザ設定の保存やショッピングカートの情報など
* サーバのファイル内にユーザログイン情報を保存

```kotlin
application.install(Sessions) {
    cookie<Session1>("Session1") // install a cookie stateless session
    header<Session2>("Session2", sessionStorage) { // install a header server-side session
        transform(SessionTransportTransformerDigest()) // sign the ID that travels to client
    }
}
```

```kotlin
install(Sessions) {
    cookie<SessionCart>("SESSION_CART_LIST") {
        cookie.path = "/shop" // Just accessible in '/shop/*' subroutes
    }
    cookie<SessionLogin>(
        "SESSION_LOGIN",
        directorySessionStorage(File(".sessions"), cached = true)
    ) {
        cookie.path = "/" // Specify cookie's path '/' so it can be used in the whole site
        transform(SessionTransportTransformerDigest()) // sign the ID that travels to client
    }
}
```

複数のセッションマッピングのためには、型も名前も _両方_ とも一意になる必要があります。
{: .note} 

## 設定
{: #configuration}

セッションの設定をいくつかの異なる方法で行うことができます

* *ペイロードがどこに保存されているか:* クライアントサイドかサーバサイドか...
* *ペイロードやセッションIDが何で送信されるか:* CookieかHeaderか...
* *どのようにシリアライズされるか:* 内部的なフォーマットを使うか、JSONを使うか、カスタムエンジンを使うか...
* *サーバのどこいペイロードが保管されているか:* インメモリかフォルダかredisか...
* *ペイロードの変換方法:* 暗号化された状態か, 認証された状態か...

セッションは様々な手段で実装可能なので、それらを広範囲に設定する設定方法があります:

* `cookie`はCookieベースの転送機能をインストールします
* `header`はHeaderベースの転送機能をインストールします 

これらの関数はcookieまたはheaderの名前を必要とします。

もし関数が`SessionStorage`という型の引数を渡された場合、セッションを保存するためのストレージがつかわれます。
そうでなければデータをcookie/headerの値内にデータをシリアライズするします。

これらの関数はオプショナルで設定のためのlambda関数を受け取ります。

cookieの場合、レシーバは`CookieSessionBuilder`で、以下のようなことができます:

* カスタムの`serializer`を指定できます
* 例えば署名・暗号化といった`transformer`の値を追加できます
* 有効期限・エンコード・ドメイン・パスなどのようなcookie設定を指定できます

Headerの場合、レシーバは`HeaderSessionBuilder`で、これにより`serializer`と`transformer`のカスタマイズができます。

`SessionStorage`を使ってサーバサイドにおけるcookieとheaderの場合、追加の設定として`identity`関数があり、
新しいセッションが作成されたときのIDの生成方法を指定できます。

## セッションの設定方法の決定
{: #configuring}

### Cookie vs Header

* [**Cookie**](/servers/features/sessions/cookie-header.html#cookies)はプレーンなHTMLアプリケーションに利用します。
* [**Header**](/servers/features/sessions/cookie-header.html#headers)は（もしクライアントにとってそちらのほうがシンプルな場合）APIやXHRリクエストに利用します。

### クライアント vs サーバー

* セッションリプレイを防ぎたい場合やよりセキュリティを強化したい場合には[**Server Cookie**](/servers/features/sessions/client-server.html#server-cookies)を使います。
  * 開発時など、サーバーを停止した後にセッションを破棄したい場合は`SessionStorageMemory`を使います。
  * 本番環境など、サーバーを再起動したあとにもセッションを残したい場合は`directorySessionStorage`を使います。
* バックエンドのストレージよりもシンプルなものを求めている場合は[**Client Cookie**](/servers/features/sessions/client-server.html#client-cookies)を使います。
  * 改ざんされることは気にせずテスト用の目的で臨機応変に変更したい場合は、プレーンな状態で使います。
  * 改ざんを防ぎたい場合は、認証やあるいはオプショナルで暗号化も一緒につけ変換し使います。
  * セッションペイロードがリプレイアタックに脆弱な場合は**絶対に使ってはいけません**。[セキュリティ例はこちら](/servers/features/sessions/client-server.html#security)。

## 動作サンプル

### Cookie内にセッションコンテンツを保存

`cookie`メソッドの第二引数でSessionStorageが渡されていないので、Cookie内にコンテンツが保存されます。

```kotlin
install(Sessions) {
    val secretHashKey = hex("6819b57a326945c1968f45236589")
    
    cookie<SampleSession>("SESSION_FEATURE_SESSION") {
        cookie.path = "/"
        transform(SessionTransportTransformerMessageAuthentication(secretHashKey, "HmacSHA256"))
    }
}
```

### セッションIDをCookie内に保存し、セッションコンテンツをインメモリに保存する
{: #SessionStorageMemory }

`SessionStorageMemory`は今時点では引数を持ちません。

```kotlin
install(Sessions) {
    cookie<SampleSession>("SESSION_FEATURE_SESSION_ID", SessionStorageMemory()) {
        cookie.path = "/"
    }
}
```

SessionStorageに似た、開発用に利用できる`SessionStorageMemory`クラスがあります。
セッションをインメモリで保持するシンプルな実装であり、サーバーをシャットダウンした際には全セッションが失われます。
また古いセッションを全く破棄しないので常にメモリ内でセッションが増え続けます。

この実装は本番環境で利用されることは想定されていません。

### Cookie内にセッションIDを保存し、セッションコンテンツをファイル内に保存
{: #directorySessionStorage }

`directorySessionStorage`関数のため、追加のアーティファクトを含める必要があります。

`compile("io.ktor:ktor-server-sessions:$ktor_version") // Required for directorySessionStorage`

```kotlin
install(Sessions) {
    cookie<SampleSession>(
        "SESSION_FEATURE_SESSION_ID",
        directorySessionStorage(File(".sessions"), cached = true)
    ) {
        cookie.path = "/" // Specify cookie's path '/' so it can be used in the whole site
    }
}
```

`io.ktor:ktor-server-sessions`アーティファクトの一部として、`directorySessionStorage`関数があり、
ディスクにセッションを保存するためにフォルダを使うセッションストレージが利用可能になります。

この関数は第一引数が`File`型であり、これがセッションを保存するフォルダです（もし存在しない場合は作成されます）

また、オプショナルなキャッシュ引数もあり、設定された場合、60秒インメモリキャッシュを保持し、
OS命令呼び出しとディスクからのセッション読み込みを防ぎます。

{% comment %}
### Storing a session id in a cookie, and storing session contents in redis
{: #redisStorage }

> <https://github.com/ktorio/ktor/pull/504>{: target="_blank"}

```kotlin
val redis = RedisClient()
install(Sessions) {
    val cookieName = "SESSION"
    val sessionStorage = RedisSessionStorage(redis, ttlSeconds = 7 * 24 * 3_600) // Sessions lasts up to 7 days
    cookie<TestSession>(cookieName, sessionStorage)
}
```

{% include artifact.html kind="class" class="io.ktor.sessions.RedisSessionStorage" artifact="io.ktor:ktor-server-session-redis:$ktor_version" %}

{% endcomment %}

## 拡張
{: #extending }

Sessionは拡張性をもたせた設計になっています。
デフォルトのセッション挙動を組み合わせたり変更したりしたいケースがあるかもしれません。

例えば、転送値に対しカスタムの暗号化や認証アルゴリズムを使うことや、セッション情報をサーバーサイドで特定のデータベースに保存することなど考えられます。

[カスタムトランスフォーマー]、 [カスタムシリアライザー]、[カスタムストレージ]を定義することができます。

[カスタムトランスフォーマー]: /servers/features/sessions/transformers.html
[カスタムシリアライザー]: /servers/features/sessions/serializers.html
[カスタムストレージ]: /servers/features/sessions/storages.html
