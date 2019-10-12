---
title: Webjar
caption: Webjarのサポート
category: servers
permalink: /servers/features/webjars.html
feature:
  artifact: io.ktor:ktor-webjars:$ktor_version
  class: io.ktor.Webjars
redirect_from:
- /features/webjars.html
ktor_version_review: 1.0.0
---

この機能は[webjar](https://www.webjars.org/)によって提供されている静的コンテンツの配信を有効化する機能です。
これを使うことでjavascriptライブラリやcssをあなたのjarファイルの一部としてパッケージすることができます。

## 機能のインストール

{: #installing }

```kotlin
    install(Webjars) {
        path = "assets" //defaults to /webjars
        zone = ZoneId.of("EST") //defaults to ZoneId.systemDefault()
    }
```

このコードは`/assets/`パスの任意のwebjarアセットを配信するよう設定しています。
`zone`引数は正とするタイムゾーンを設定しており、
これは([Conditional Header](/servers/features/conditional-headers.html) Featureもインストールされている場合は)`Last-Modified`ヘッダーに使われ、
キャッシュ機能のサポートするのに使われます。

{% include feature.html %}

## バージョニングのサポート

Webjarを使うことで開発者は依存ライブラリについて、
テンプレート内でのロードパスを変更する必要無しにバージョンを変更することができます。

`org.webjars:jquery:3.2.1`をインポートしている場合を想定してください。
次のHTMLコードをインポートするために利用できます。

```html
<head>
  <script src="/webjars/jquery/jquery.js"></script>
</head>  
```

バージョンを指定する必要はありません。そのため依存ライブラリのバージョンを更新する選択をした場合、テンプレートの変更の必要はありません。