---
title: コードスタイル
caption: コードスタイル
category: quickstart
permalink: /quickstart/code-style.html
toc: false
ktor_version_review: 1.0.0
---

## 公式コーディング規約

Ktorは他の公式のKotlinライブラリと同様に[Kotlin 公式コーディング規約](https://kotlinlang.org/docs/reference/coding-conventions.html)に従います。

`gradle.properties`に`kotlin.code.style=official`を追加することで、公式のコーディング規約を使用できます。

## オンデマンドインポート (`*`)

公式のコーディング規約では、インポートの方式の規定はありません。
IntelliJ IDEA のデフォルト設定では、同一パッケージから5つ以上のシンボルをインポートすると、動的にインポート文を `*` に置き換えますが、 Ktor や JetBrains 製のライブラリは、常にオンデマンドインポート (`*`) を使用することを推奨しています。

特定のクラスをインポートした際、多くの場合でそのクラス向けに定義された拡張関数や拡張プロパティも使いたくなるからです。
演算子拡張関数に対しては特に威力を発揮します。

`Preferences... -> Editor -> Code Style -> Kotlin -> Imports` からインポートの設定を変更できます。

![](/quickstart/code-style/code-style-imports.png)

