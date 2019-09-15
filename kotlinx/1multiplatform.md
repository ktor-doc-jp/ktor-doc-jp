---
title: マルチプラットフォーム
caption: マルチプラットフォーム
category: kotlinx
permalink: /kotlinx/multiplatform.html
toc: true
ktor_version_review: 1.0.0
---

Kotlin Multi-Platform プロジェクト(Kotlin MPPとも呼ばれます)。

Kotlin 1.2からmultiplatformのexperimentalなサポートがスタートしました。
1.3においてもまだexperimentalですが、大きな改善がなされています。

その背後にあるアイデアは、全プラットフォームで利用可能な共通コードと、プラットフォーム固有のコードを分けて書くことです。

MultiplatformプロジェクトはKotlin言語にいくつかの新たなキーワードを必要とします。`expect`と`actual`です。

* `expect`はcommonプロジェクトにおいて利用可能で、内容の無いAPIを定義します。これに対しプラットフォーム固有の実装を持つことになります。
* `actual`はcommonプロジェクト以外（JVM, JS, Native）において利用可能で、サポートするプラットフォームごとに`expect`の構造と一致させて書く必要があります。

**参考: <https://kotlinlang.org/docs/reference/multiplatform.html>**
