---
title: 圧縮
caption: HTTP Compression機能の有効化
category: servers
permalink: /servers/features/compression.html
feature:
  artifact: io.ktor:ktor-server-core:$ktor_version
  class: io.ktor.features.Compression
redirect_from:
- /features/compression.html
ktor_version_review: 1.0.0
---

Compression Featureは送信するコンテンツをgzipやdeflateやカスタムエンコーダを使い圧縮する機能を提供し、
それを使うことでレスポンスサイズを減らすことができます。

```kotlin
install(Compression)
```

{% include feature.html %}

## 設定

設定処理を行うブロックが省略された場合、デフォルトの設定が利用されます。
設定には以下のエンコーダが含まれています:
 
 * gzip
 * deflate
 * identity

特定のエンコーダを選択したい場合は、設定処理を行うブロックで指定する必要があります:

```kotlin
install(Compression) {
    gzip()
}
```

各エンコーダーは、priorityとその他条件を設定することができます:

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

エンコーダーはHTTPリクエストの`Accept-Encoding`で指定されたqualityと、指定されたpriorityでソートされます。
すべての条件を満たすエンコーダーのうち初めに定義されたものが選択されます。

上の例において`Accept-Encoding`がqualityを指定しなかった場合、サイズが1K未満のものについては`gzip`が選択され、
それ以外のものについては`deflare`エンコーダーが利用されます。

いくつかの典型的な条件が簡単に利用できるようになっています:

* `minimumSize` – 圧縮可能な最小レスポンスサイズ
* `matchContentType` – 圧縮すべきコンテントタイプ
* `excludeContentType` – 圧縮しないコンテントタイプ

lambdaを指定することでカスタムの条件を使うこともできます:

```kotlin
gzip {
    condition {
        parameters["e"] == "1"
    }
}
```

## HTTPSによるセキュリティ

HTTPSで圧縮を使った場合は[BREACH](https://en.wikipedia.org/wiki/BREACH){:target="_blank"}攻撃に対し脆弱です。
この攻撃は、悪意ある攻撃者が暗号化されたHTTPSページから1分に満たない時間で秘密情報（セッション・認証トークン・パスワード・クレジットカードなど）を推測するものです。

この攻撃は以下のようにして軽減できます:

* HTTP圧縮機能を完全にOFFにする（パフォーマンスに影響は出るかもしれません）
* ユーザの入力情報（GET, POSTリクエスト内容やHeader/Cookieパラメータ）を秘密情報（`Set-Cookie`のセッションIDなども含む）を含むレスポンス（HeaderやBody）の一部に入れない
* ランダムな量のバイト列を追加する。例えばHTMLページに単に`<!-- 100~500 random_bytes !-->`を追加することで、現実的な時間で秘密情報を推測することが攻撃者にとって難しくなります。
* ウェブサイトが **完全にHTTPSでありHSTSが有効化されている** ことを保証し、Referrerページをチェックするためconditional headerを追加する（もしHTTPSを使わないシングルページだった場合は、悪意ある攻撃者はReferrerとして同じドメインを使いページにコードを挿入することができます）
* [CSRF](https://en.wikipedia.org/wiki/Cross-site_request_forgery){:target="_blank"}防御をページに導入する

```kotlin
application.install(Compression) {
    gzip {
        condition {
            // @TODO: Check: this is only effective if your website is completely HTTPS and has HSTS enabled. 
            request.headers[HttpHeaders.Referrer]?.startsWith("https://my.domain/") == true
        }
    }
}
```

TL;DR; Even when HTTPS prevents an eavesdropper to know the content of a request, it does not hide the response length.
So one of your users could be connecting to an evil access point, for example by connecting to a public network
or one with a well-known password, or a private network with an Evil Twin. That access point can intercept all the
encrypted messages and measure the length. Then can modify any non-https connection (or social engineer the user to
access an https page controlled by the attacker) to inject a javascript or place images pointing to the vulnerable
page mutating an input (get, post or header parameters) that are reflected in either the headers or the response body,
then the access point can measure the length of the responses to guess a secret with as little as 100 to 10000 requests
that are forced to be done by your browser with either the javascript or image requests without the user ever noticing.
{: .note.security }


## 拡張

カスタムのエンコーダーを作りたい場合は、`CompressionEncoder`インターフェースを実装し、設定を行う関数を作ればよいです。
コンテンツは`ReadChannel`か`WriteChannel`として渡されるので、これらを圧縮することができます。
`GzipEncoder`をエンコーダーの例としてご参照ください。
