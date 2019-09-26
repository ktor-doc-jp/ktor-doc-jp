---
title: Application
category: servers
permalink: /servers/application.html
caption: Applicationとは? 
ktor_version_review: 1.2.1
---

Ktorサーバーアプリケーションは、HTTP/S 1.x/2.x、WebSocketリクエストを処理するための
[ルーティング](/servers/features/routing.html)、[セッション](/servers/features/sessions.html)、[圧縮](/servers/features/compression.html)などの
[Features](#features)をインストールするロジックを備えた[モジュール](#modules)で構成されるサーバーエンジンを使用して、1つ以上のポートをリッスンするカスタムプログラムです。

Ktorは、[生のソケットを処理する機能](/servers/raw-sockets.html)も提供しますが、アプリケーションおよびそのパイプラインの一部としては提供しません。
{: .note}

**目次:**

* TOC
{:toc}

## Application

`Application`インスタンスは、Ktorアプリケーションのメインユニットです。
リクエストを受信すると(リクエストはHTTP、HTTP/2、またはWebSocketの場合があります)、`ApplicationCall`に変換され、`Application`が所有するパイプラインを通過します。
パイプラインは、以前にインストールされた1つ以上のインターセプターで構成され、リクエストの処理を終了するルーティング、圧縮などの特定の機能を提供します。

通常、Ktorプログラムは、[Features](#features)をインストールおよび構成する[modules](#modules)を介してアプリケーションパイプラインを構成します。

[ライフサイクル](/servers/lifecycle.html)セクションで、パイプラインに関する詳細情報を読むことができます。
{: .note}

## アプリケーションコール

[アプリケーションコール](/servers/calls.html)に関するページをご覧ください。

## Features

Featuresは、パイプラインにインストールおよび構成できるシングルトン(通常はコンパニオンオブジェクト)です。
Ktorにはいくつかの標準でFeatureが含まれていますが、独自のFeatureを追加したりコミュニティから他のFeatureを追加したりできます。
アプリケーション自体や特定のルートなど、任意のパイプラインに機能をインストールできます。

[Features](/servers/features.html)の詳細については、専用ページをご覧ください。
{: .note}

## モジュール
{: #modules}

Ktorモジュールは、サーバーパイプラインの構成、Featuresのインストール、ルートの登録、リクエストの処理などを担当するApplicationクラスを受け取るユーザー定義関数です。

サーバーの起動時にロードするモジュールを[`application.conf`ファイル](/servers/configuration.html#hocon-file)で指定する必要があります。

単純なモジュール関数は次のようになります。

{% capture main-kt %}
```kotlin
package com.example.myapp

fun Application.mymodule() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
}
```
{% endcapture %}

{% include tabbed-code.html
    tab1-title="Main.kt" tab1-content=main-kt
    no-height="true"
%}

もちろん、モジュール関数をいくつかの小さな関数またはクラスに分割できます。

モジュールは、完全修飾名(クラスの完全修飾名とドット(.)で区切られたメソッド名)によって参照されます。

したがって、この例では、モジュールの完全修飾名は次のようになります。

```kotlin
com.example.myapp.MainKt.mymodule
```

`mymodule`はApplicationクラスの拡張メソッドです(`Application`は*レシーバー*です)。
Kotlinはトップレベル関数として定義されているため、`Kt`サフィックス(`FileNameKt`)を持つJVMクラスを作成し、最初のパラメーターとしてレシーバーを持つ静的メソッドとして拡張メソッドを追加します。
この場合、クラス名は`com.example.myapp`パッケージの`MainKt`であり、Javaメソッドシグネチャは`static public void mymodule(Application app)`になります。
{: .note}

## 関連項目

- [アプリケーションコール](/servers/calls.html)
- [ライフサイクル](/servers/lifecycle.html)
- [アプリケーション設定](/servers/configuration.html)
- [パイプライン](/advanced/pipeline)
