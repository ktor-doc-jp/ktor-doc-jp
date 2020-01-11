---
title: テスト 
category: servers
permalink: /servers/testing.html
caption: サーバアプリケーションのテスト
redirect_from:
  - /application/testing.html
---

Ktorはテスタブルなアプリケーションの作成ができるように設計されています。
そしてもちろん、Ktorインフラストラクチャ自体がunitテスト、integrationテスト、stressテストによって念入りにテストされています。
このセクションでは、どのようにあなたのアプリケーションをテストするかについて学びます。

**目次:**

* TOC
{:toc}

## TestEngine

Ktorは特殊な種類のエンジンとして`TestEngine`を持っています。
このエンジンはwebサーバを作成せず、socketをbindせず、本物のHTTPリクエストは行わないものです。
代わりに、このエンジンは内部的なメカニズムに直接フックし、`ApplicationCall`を直接処理します。
これによりHTTPの詳細部分の処理時間が無くなり、素早いテストの実行ができます。
アプリケーションロジックのテストは問題なく行うことができ、また同様にintegrationテストのセットアップも行うことができます。

簡単に全体像を見てみましょう:

* `ktor-server-test-host`への依存を`test`スコープに追加します
* JUnitテストクラスとテスト関数を作成します
* `withTestApplication`を使い、アプリケーションのテスト環境をセットアップします
* `handleRequest`関数を使い、アプリケーションにリクエストの送信をしその結果の検証を行います

## post/putボディーの構築

### `application/x-www-form-urlencoded`

リクエストを構築するには、`Content-Type`ヘッダーを追加する必要があります:

```kotlin
addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
```

そして例えば`setBody`メソッドを呼び出すことによって、`bodyChannel`をセットします:

```kotlin
setBody("name1=value1&name2=value%202")
```

Ktorはkey/valueのペア`List`からform urlencodedを構築する拡張関数を提供しています:

```kotlin
fun List<Pair<String, String>>.formUrlEncode(): String
```

以上より、urlencodedのPOSTリクエストを構築するための完全な例は例えば以下のようになります:

```kotlin
val call = handleRequest(HttpMethod.Post, "/route") {
   addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
   setBody(listOf("name1" to "value1", "name2" to "value2").formUrlEncode())
}
```

### `multipart/form-data`

巨大なファイルをアップロードするときには、multipartエンコードを通常利用します。
これを使うことで、処理することなしにファイル全体を送信することができます。
Ktorのテストホストは`setBody`拡張関数を提供しており、この種類のペイロードを構築することができます。
例えば:

```kotlin
val call = handleRequest(HttpMethod.Post, "/upload") {
    val boundary = "***bbb***"

    addHeader(HttpHeaders.ContentType, ContentType.MultiPart.FormData.withParameter("boundary", boundary).toString())
    setBody(boundary, listOf(
        PartData.FormItem("title123", { }, headersOf(
            HttpHeaders.ContentDisposition,
            ContentDisposition.Inline
                .withParameter(ContentDisposition.Parameters.Name, "title")
                .toString()
        )),
        PartData.FileItem({ byteArrayOf(1, 2, 3).inputStream().asInput() }, {}, headersOf(
            HttpHeaders.ContentDisposition,
            ContentDisposition.File
                .withParameter(ContentDisposition.Parameters.Name, "file")
                .withParameter(ContentDisposition.Parameters.FileName, "file.txt")
                .toString()
        ))
    ))
}
```

## テストにおいて、設定プロパティを定義

テストにおいて、`application.conf`を使う代わりに、`MapApplicationConfig.put`メソッドを利用して設定プロパティを定義します:

```kotlin
withTestApplication({
    (environment.config as MapApplicationConfig).apply {
        // Set here the properties
        put("youkube.session.cookie.key", "03e156f6058a13813816065")
        put("youkube.upload.dir", tempPath.absolutePath)
    }
    main() // Call here your application's module
})
```

## HttpsRedirect Feature

HttpsRedirectはテストの挙動を変更します。
[HttpsRedirect Featureのテストセクション](/servers/features/https-redirect.html#testing)を確認してください。

## session/cookieを保持する連続するリクエストのテスト
{: #preserving-cookies }

`Cookie`情報を保持する連続するリクエストのテストも、`cookiesSession`メソッドを利用することで簡単に行なえます。
このメソッドはCookieを保持するセッションコンテキストを定義し、
`CookieTrackerTestApplicationEngine.handleRequest`拡張関数をコンテキストのもとリクエスト実行するために利用可能にします。

例:

```kotlin
@Test
fun testLoginSuccessWithTracker() = testApp {
    val password = "mylongpassword"
    val passwordHash = hash(password)
    every { dao.user("test1", passwordHash) } returns User("test1", "test1@test.com", "test1", passwordHash)

    cookiesSession {
        handleRequest(HttpMethod.Post, "/login") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("userId" to "test1", "password" to password).formUrlEncode())
        }.apply {
            assertEquals(302, response.status()?.value)
            assertEquals("http://localhost/user/test1", response.headers["Location"])
            assertEquals(null, response.content)
        }

        handleRequest(HttpMethod.Get, "/").apply {
            assertTrue { response.content!!.contains("sign out") }
        }
    }
}
```

Note: `cookiesSession`はKtor自身には含まれていませんが、以下のボイラープレートを追加することで利用できます:

```kotlin
fun TestApplicationEngine.cookiesSession(
    initialCookies: List<Cookie> = listOf(),
    callback: CookieTrackerTestApplicationEngine.() -> Unit
) {
    callback(CookieTrackerTestApplicationEngine(this, initialCookies))
}

class CookieTrackerTestApplicationEngine(
    val engine: TestApplicationEngine,
    var trackedCookies: List<Cookie> = listOf()
)

fun CookieTrackerTestApplicationEngine.handleRequest(
    method: HttpMethod,
    uri: String,
    setup: TestApplicationRequest.() -> Unit = {}
): TestApplicationCall {
    return engine.handleRequest(method, uri) {
        val cookieValue = trackedCookies.map { (it.name).encodeURLParameter() + "=" + (it.value).encodeURLParameter() }.joinToString("; ")
        addHeader("Cookie", cookieValue)
        setup()
    }.apply {
        trackedCookies = response.headers.values("Set-Cookie").map { parseServerSetCookieHeader(it) }
    }
}
```


## 依存関係に関する例

[ktor-samples-testable](https://github.com/ktorio/ktor-samples/tree/master/feature/testable)にある
アプリケーションテストの完全な例を参考にしてください。
また、ほとんどの[`ktor-samples`](https://github.com/ktorio/ktor-samples)モジュールは特定の機能をどのようにテストするのかの例を提供してくれます。

いくつかのケースにおいては、サービスや依存ライブラリを必要とします。
グローバルにそれらを保存する代わりに、サービスへの依存を受け取る別々の関数を作成することをおすすめします。
そうすることで、異なる依存関係（モックされているかもしれない）をテストにおいて渡すことができます。

{% capture build-gradle %}
```groovy
// ...
dependencies {
    // ...
    testCompile("io.ktor:ktor-server-test-host:$ktor_version")
}
```
{% endcapture %}

{% capture module-kt %}
```kotlin
fun Application.testableModule() {
    testableModuleWithDependencies(
        random = SecureRandom()
    )
}

fun Application.testableModuleWithDependencies(random: Random) {
    routing {
        get("/") {
            call.respondText("Random: ${random.nextInt(100)}")
        }
    }
}
```
{% endcapture %}

{% capture test-kt %}
```kotlin
class ApplicationTest {
    class ConstantRandom(val value: Int) : Random() {
        override fun next(bits: Int): Int = value
    }

    @Test fun testRequest() = withTestApplication({
        testableModuleWithDependencies(
            random = ConstantRandom(7)
        )
    }) {
        with(handleRequest(HttpMethod.Get, "/")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("Random: 7", response.content)
        }
        with(handleRequest(HttpMethod.Get, "/index.html")) {
            assertFalse(requestHandled)
        }
    }
}
```
{% endcapture %}

{% include tabbed-code.html
    tab1-title="test.kt"      tab1-content=test-kt
    tab2-title="module.kt"    tab2-content=module-kt
    tab3-title="build.gradle" tab3-content=build-gradle
%}
