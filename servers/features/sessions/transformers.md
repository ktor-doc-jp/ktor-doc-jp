---
title: トランスフォーマー
caption: セッショントランスフォーマー
category: servers
redirect_from:
- /features/sessions/transformers.html
ktor_version_review: 1.0.0
---

{::options toc_levels="1..3" /}

**目次:**

* TOC
{:toc}

## 標準のトランスフォーマー

### SessionTransportTransformerDigest
{: #SessionTransportTransformerDigest}

`SessionTransportTransformerEncrypt`はセッション転送時のトランスフォーマーを提供し、
これを使うことでソルト付きでのペイロードのハッシュ化や、その検証をすることができます。
`SHA-256`をデフォルトのハッシュアルゴリズムとして利用していますが、変更することもできます。
これはペイロードを暗号化しませんが、ソルトなしには他の人が値を変更できないようにします。

```kotlin
// REMEMBER! Change this string and store them safely
val salt = "my unity salt string"
cookie<TestUserSession>(cookieName) {
    transform(SessionTransportTransformerDigest(salt))
}
```

このモードを使うとき、セッションの実際のコンテンツをクライアントに対し、CookieであれHeaderであれ、また平文であれ変換された形式であれ送信することができます。

このモードは"サーバレス”と考えることができます。
そのためサーバサイドには何も保存する必要がなく、セッションを保存・保持するのはクライアントだけの責務だからです。
これはバックエンドをシンプルにしますが、このモードで動く場合セキュリティ面で何が問題となるのかを知っておく必要があります。

このモードにおいては、`header`または`cookie`メソッドを`install(Sessions)`ブロック内で、cookieまたはheaderの名前を１つ引数として渡してやり呼び出すだけでいいです。

`header`、`cookie`ブロック内で、`transform`メソッドを呼ぶかどうかの選択ができます。
呼ぶことで、送信される値を変換し、例えば認証したり暗号化したりすることができます。

```kotlin
install(Sessions) {
    val secretHashKey = hex("6819b57a326945c1968f45236589")

    cookie<SampleSession>("SESSION_FEATURE_SESSION") {
        cookie.path = "/"
        transform(SessionTransportTransformerMessageAuthentication(secretHashKey, "HmacSHA256"))
    }
}
```

* transformせずにセッションを送信した場合、他者にセッションのコンテンツが完全に見え、改ざんすることができます。
* 認証付きのtransformをしセッションを送信した場合、他者はコンテンツをみることができますが、ハッシュ化のためのシークレットキーを安全に保管し、安全なアルゴリズムを使っている限りは改ざんすることはできません。
  古いセッション文字列を使って古い状態に戻すこともできます
* 暗号化付きのtransformをしセッションを送信した場合、他者はコンテンツを見ることができず、改ざんすることはできません。
  しかし、過去の状態に戻すこと対しては依然脆弱性はあります。
{: .note.security }
  
タイムスタンプかnonceを保存することはできますが、
セッションの有効期限の制限やその検証をサーバ側で行う必要が出てくるため、このモードの利点を減らしてしまいます。

経験則としては、このモードは**古いセッション状態が使われうるセキュリティ上の懸念がないときだけ**利用してください。
そしてもしセッションをユーザのログインに利用している場合、**transform時に少なくとも認証は行うようにしてください**。
そうでなければ簡単にセッションコンテンツにアクセスできてしまうためです。

またもしセキュアなキーが危険にさらされている場合、キーを持つ人が任意のセッションペイロードを作成でき任意のユーザになりすませる可能性があることを
頭にいれておいてください。

キーを変更することは全ユーザの全セッションを無効化することも重要なので覚えておいてください。

### SessionTransportTransformerMessageAuthentication
{: #SessionTransportTransformerMessageAuthentication}

`SessionTransportTransformerMessageAuthentication`はセッション転送時の変換処理を提供します。
ペイロードに対する認証ハッシュとその検証機能を提供します。
SessionTransportTransformerDigestに似ていますが、HMACを利用しています。
`HmacSHA265`をデフォルトの認証アルゴリズムとして使用していますが、変更することもできます。
ペイロードを暗号化はしませんが、キーなしには改ざんできないようにできます。

```kotlin
// REMEMBER! Change this string and store them safely
val key = hex("03515606058610610561058")
cookie<TestUserSession>(cookieName) {
    transform(SessionTransportTransformerMessageAuthentication(key))
}
``` 

### SessionTransportTransformerEncrypt
{: #SessionTransportTransformerEncrypt}

`SessionTransportTransformerEncrypt`はセッション転送時の変換処理を提供します。
ペイロードの暗号化とその認証処理を提供します。
デフォルトでは`AES`と`HmacSHA256`を利用していますが、設定変更することができます。
アルゴリズムと互換性があるサイズの暗号化キーと認証キーが必要です。

```kotlin
// REMEMBER! Change ALL the digits in those hex numbers and store them safely
val secretEncryptKey = hex("00112233445566778899aabbccddeeff") 
val secretAuthKey = hex("02030405060708090a0b0c")
cookie<TestUserSession>(cookieName) {
    transform(SessionTransportTransformerEncrypt(secretEncryptKey, secretAuthKey))
}
``` 

## カスタムTransportTransformer
{: #extending-transport-transformers}

Session APIは`SessionTransportTransformer`インターフェースを提供しており、以下のようになります:

```kotlin
interface SessionTransportTransformer {
    fun transformRead(transportValue: String): String?
    fun transformWrite(transportValue: String): String
}
```

これらのトランスフォーマーはペイロードの暗号化や認証や変換に使えます。
インターフェースを実装し、普通にトランスフォーマーとして使うことができます:

```kotlin
cookie<MySession>("NAME") {
    transform(MtSessionTransformer)
}
```

`SessionTransportTransformer`はセッション値を変換することができ、リクエストとともに送信されます。
入力としてトランスポートされた値またはその変換のいずれかを持つことができます。
2つのメソッドから構成されており、１つは変換を適用する`transformWrite`と、もう１つはその変換を解除する`transformRead`です。
入力も出力も両方とも文字列です。
通常`transformWrite`は常に動作しますが、`transformRead`は入力が不正だった場合にはnullをreturnして失敗します。

```kotlin
interface SessionTransportTransformer {
    fun transformRead(transportValue: String): String?
    fun transformWrite(transportValue: String): String
}
``` 
