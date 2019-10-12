---
title: アーティファクト
caption: アーティファクト一覧
permalink: /quickstart/artifacts.html
category: quickstart
redirect_from:
  - /artifacts.html
ktor_version_review: 1.0.1
---

Ktorはいくつかのモジュールに分割されており、必要な機能だけ依存関係に追加することができます。
典型的なKtorアプリケーションは `ktor-server-core` を必要とし、自己ホスト型とするか別途アプリケーションサーバを用いるかによって、
必要なモジュールが変わります。

Ktorのすべてのアーティファクトは `io.ktor` パッケージ配下に属し、JCenterとMaven Centralから取得可能です。
プレリリース版は[Bintray](https://bintray.com/kotlin/ktor)から取得可能です。

[![Download](https://api.bintray.com/packages/kotlin/ktor/ktor/images/download.svg?version={{site.ktor_version}})](https://bintray.com/kotlin/ktor/ktor/{{site.ktor_version}})
    
Ktorのモジュールはいくつかのグループに分類されます。

* `ktor-server` は、Netty、Jetty、Tomcat、および汎用サーブレットといった複数のエンジン上でKtorアプリケーションが動作するためのモジュール群です。
また、実サーバを起動させることなくアプリケーションのテストが可能なTestEngineも含んでいます。
  * `ktor-server-core` : ほとんどすべてのアプリケーションで必要な API と実装を持つコアパッケージ
  * `ktor-server-jetty` : Jetty サーバ用、または Jetty 内包アプリケーション用
  * `ktor-server-netty` : Netty 内包アプリケーション用
  * `ktor-server-tomcat` : Tomcat サーバ用
  * `ktor-server-servlet` : Jetty と Tomcat でも用いられている、汎用サーブレットコンテナ用
  * `ktor-server-test-host` : 実アプリケーションの起動不要な、テスト実行環境用
* `ktor-features` : 一部のアプリケーションで必要とされる、追加機能のモジュール群
  * `ktor-auth` : Basic 認証、 Digest 認証、 Forms 認証、 OAuth 1a, OAuth 2 といった、様々な [認証システム](/servers/features/authentication.html) を提供
  * `ktor-auth-jwt` : [JWT](/servers/features/authentication/jwt.html) (JSON Web Token) 認証を提供
  * `ktor-auth-ldap` : [LDAP](/servers/features/authentication/ldap.html) を用いた認証を提供
  * `ktor-freemarker` : テンプレートエンジン [FreeMaker](/servers/features/templates/freemarker.html) 向けの機能を提供
  * `ktor-velocity` : テンプレートエンジン [Velocity templates](/servers/features/templates/velocity.html) 向けの機能を定期用
  * `ktor-gson` : [Gson](/servers/features/content-negotiation/gson.html) と JSON コンテンツネゴシエーションを提供
  * `ktor-jackson` [Jackson](/servers/features/content-negotiation/jackson.html) と JSON コンテンツネゴシエーションを提供
  * `ktor-html-builder` : テンプレートエンジン [kotlinx.html builders](/servers/features/templates/html-dsl.html) 向けの機能を提供
  * `ktor-locations` : [typed locations](/servers/features/locations.html) を提供 (experimental)
  * `ktor-metrics` : サーバの [メトリクス](/servers/features/metrics.html) を取得するための機能を提供
  * `ktor-server-sessions` : [サーバに保存された Stateful Session](/servers/features/sessions.html) を利用するための機能を提供
  * `ktor-websockets` : [Websockets](/servers/features/websockets.html) 用の機能を提供
* `ktor-client` : [HTTP クライアント](/clients/http-client.html) モジュール群
  * `ktor-client-core` : ほとんどすべての HTTP クライアント API で必要とさせるモジュール
  * `ktor-client-apache` : Apache asynchronous HttpClient を提供
  * `ktor-client-cio` : Kotlin Coroutine ベースの非同期 I/O を行う HttpClient を提供
  * `ktor-client-jetty` : [Jetty HTTP client](https://www.eclipse.org/jetty/javadoc/current/org/eclipse/jetty/http2/client/HTTP2Client.html) を提供
  * `ktor-client-okhttp` : クライアントで利用される HTTP クライアント [OkHttp](https://square.github.io/okhttp/) を提供
  * `ktor-client-auth-basic` : クライアントサイドの [認証](/clients/http-client/features/auth.html) 機能を提供
  * `ktor-client-json` : クライアントサイドの [JSON コンテンツネゴシエーション](/clients/http-client/features/json-feature.html) 機能を提供
* `ktor-network` : クライアントサイド、サーバサイド両用の、TCP/UDP を用いた [生ソケット通信](/servers/raw-sockets.html) 機能を提供
  * `ktor-network-tls` 生ソケット通信用の TLS 通信向けの機能を提供
 
各プロジェクト管理ツール向けのセットアップ手順を参照してください。

* [Maven](/quickstart/quickstart/maven.html)
* [Gradle](/quickstart/quickstart/gradle.html)
* [オンラインでのプロジェクト生成ツール](/quickstart/generator.html)

