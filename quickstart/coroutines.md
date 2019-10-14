---
title: Coroutines
category: quickstart
permalink: /quickstart/coroutines.html
caption: Coroutines
redirect_from:
  - /advanced/kotlinx.coroutines.html
ktor_version_review: 1.0.0
---

Ktor は Kotlin 1.3 で stable になった Coroutine を多用しています。

Coroutine は Kotlin が言語レベルで提供している機能です。 (`suspend` 関数とも呼ばれます。)
従来のコールバックベースのアプローチの代わりに Coroutine を用いることで、非同期処理が簡潔かつ直感的に書けるようになります。

他のモダンな言語では、 await-async と呼ばれる具象的な機構を提供しています。
Kotlin のアプローチはより汎用的で柔軟であり、非同期関数 (`suspend` 関数) を呼び出す際のデフォルトの動作は呼び出し側も同期的に処理されるため、
冗長度が低くエラーが発生しにくいです。

Ktor は JetBrains 製の標準ライブラリである [kotlinx.coroutines](/kotlinx/coroutines.html) を使用しています。

Ktor は十分に非同期的で徹底的に Coroutine を使用するため、 Coroutine の概念をしっかり理解してから利用することをおすすめします。