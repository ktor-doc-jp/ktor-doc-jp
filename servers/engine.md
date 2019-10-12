---
title: 起動
caption: サーバアプリケーションの起動
category: servers
permalink: /servers/engine.html
---

Ktor のアプリケーションは Apache Tomcat などのアプリケーションサーバにデプロイしたり、Jetty等を内包した上でセルフホスティングすることもできます。
このセクションでは、外部サーバ等で Ktor アプリケーションを起動する方法を説明します。

**目次**

* 目次
{:toc}

## 外部ホスト上でのアプリケーションの起動

Apache Tomcat などのように独立して管理されるホストアプリケーション上で Ktor アプリケーションを起動する場合は、
アプリケーションの起動方法を Ktor に伝えるために、 `application.conf` ファイルが必要になります。

### 設定方法

リソースディレクトリ内に `application.conf` を作成し、下記を記載します。

```kotlin
ktor {
    deployment {
        port = 8080
    }

    application {
        modules = [ my.company.MyApplication.ApplicationKt.main ]
    }
}
```

`my.company.MyApplication` をあなたのアプリケーションのパッケージに置換し、
`ApplicationKt` を `Application.main` 関数を持つファイル名に置換してください。

### ホストアプリケーションへのデプロイ方法

// TODO 

## IDE 上でアプリケーションを実行

IntelliJ IDEA のような開発環境上でのアプリケーションの起動する場合は、開発用エンジンを用います。

#### IntelliJ IDEA 

1. "Application" テンプレートを用いて Run Configuration を新規作成
2. 使用したいエンジンに対応したクラスを Main クラスとして指定
  * Netty: `io.ktor.server.netty.EngineMain` 
  * Jetty: `io.ktor.server.jetty.EngineMain` 
3. 使用するモジュールを指定
4. 名前をつけて Configuration を保存

設定を保存したら、外部のアプリケーションサーバの構築やコンテナへのデプロイをせずとも、IntelliJ IDEA 上で開発やデバッグ目的で
あなたのアプリケーションを起動できるようになります。

[設定](configuration)ページも参照してください。

## オートリロードの使用

Ktor はクラスファイルへの変更が検出されたとき、すなわちアプリケーションをビルドしたときに、アプリケーションを自動的にリロードできます。
`application.conf` に `watch` の設定を追加することで、有効化できます。

```groovy
ktor {
    deployment {
        port = 8080
        watch = [ my.company ]
    }

    …
}
```

[オートリロード](/servers/autoreload.html)ページに詳細な情報があります。
