---
title: Feature
caption: アプリケーション機能の拡張
category: servers
permalink: /servers/features.html
children: /servers/features/
keywords: installing features install features
redirect_from:
- /features/index.html
- /servers/features/index.html
ktor_version_review: 1.0.0
---

Ktorアプリケーションはだいたいの場合いくつかのFeatureから構成されています。
Featureは「リクエスト・レスポンスのパイプラインに挿入される機能」として考えることができます。
通常、アプリケーションは`DefaultHeaders`（ヘッダーをすべての送信レスポンスに付与する機能）、`Routing`（ルーティングの定義とリクエストのハンドリングを行えるようにする機能）のようないくつかの機能を持っています。

**目次:**

* TOC
{:toc}

## Feature一覧

{% include list-children.html %}

## インストール

Featureは`install`関数を呼び出すことで、[Application](/application)へとインストールされます。

```kotlin
fun Application.main() {
    install(DefaultHeaders) 
    install(CallLogging)
    install(Routing) { 
        get("/") { 
            call.respondText("Hello, World!")  
        }
    }
}
```

`Routing`のような共通的に利用される傾向のあるFeatureはヘルパー関数があります。
それは`Application`の拡張関数として定義されているため、コードをより読みやすく書くことができます。
例えば、以下のように書く代わりに:

```kotlin
    install(Routing) {
        get("/") {
            call.respondText("Hello, World!")
        }
    }
```

シンプルに以下のように書くことができます:

```kotlin
    routing {
        get("/") {
            call.respondText("Hello, World!")
        }
    }
```

## ビルトインFeature

Ktorには、アプリケーションにインストール可能なたくさんの既製Featureがあります:

いくつかのFeatureは追加の依存関係のプロジェクトへの追加を必要とします。詳細はFeatureページをご覧ください。
{: .note }

## カスタムFeature

あなた自身のFeatureを開発し、Ktorアプリケーション間でそれを使いまわすことができます。
[発展的なFeatures](/advanced/features)をご覧ください。
