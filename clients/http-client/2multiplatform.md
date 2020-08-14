---
title: マルチプラットフォーム
category: clients
permalink: /clients/http-client/multiplatform.html
caption: マルチプラットフォーム向け HTTP クライアント
ktor_version_review: 1.2.0
---

Kotlin の試験的な [マルチプラットフォーム対応](https://kotlinlang.org/docs/reference/multiplatform.html) を用いて、Http クライアントを複数のプラットフォームに対応させています。
Kotlin のマルチプラットフォーム対応については、 [Kotlin 1.2](https://blog.jetbrains.com/kotlin/2017/11/kotlin-1-2-released/) で紹介されています。

現時点では、 JVM 、 Android 、 iOS 、 JS 、そしてネイティブに対応しています。

## 全プラットフォーム共通

複数のプラットフォーム間で実装を共有するような [マルチプラットフォームプロジェクト](https://kotlinlang.org/docs/reference/multiplatform.html) の場合、共通モジュールとして作成することができます。
共通モジュールは API を利用するだけで、全プラットフォームで利用可能になります。
Ktor HTTP クライアントはそのようなプロジェクトに適用できる共通モジュールを提供します。

```kotlin
dependencies {
    implementation("io.ktor:ktor-client-core:$ktor_version")
    // ...
}
```

## JVM
JVM 上で Ktor を利用する場合は、 [JVM 向けエンジンのうちの1つ](/clients/http-client/engines.html#jvm) を依存ライブラリとして追加 (`build.gradle` または `build.gradle.kts` に追記) する必要があります。

## Android

Android エンジンを利用するには、依存ライブラリを追加 (`build.gradle` または `build.gradle.kts` に追記) する必要があります。

```kotlin
dependencies {
    implementation("io.ktor:ktor-client-android:$ktor_version")
    // ...
}
```

Android Studio または Gradle を利用してプロジェクトをビルドすることができます。

## iOS

iOS の場合、 [Kotlin/Native](https://github.com/JetBrains/kotlin-native) を使用する必要があり、 Android と同様に `dependencies` に依存ライブラリとして追加する必要があります。

```kotlin
dependencies {
    implementation("io.ktor:ktor-client-ios:$ktor_version")
    // ...
}
```

iOS の場合、予め `.framework` として作成します。
その上で、アプリケーションのプロジェクト (Swift または Objective-C で書かれた普通の XCode プロジェクト) に、作成したフレームワークを取り込みます。

したがって、まず Gralde タスクを用いて Kotlin/Native で作成されたフレームワークをビルドし、その上で XCode プロジェクトで開く必要があります。

## Javascript

ブラウザまたは node.js のアプリケーションの場合は、 [Kotlin/Js](https://kotlinlang.org/docs/tutorials/javascript/kotlin-to-javascript/kotlin-to-javascript.html) を使用する必要があります。

```kotlin
dependencies {
    implementation("io.ktor:ktor-client-js:$ktor_version")
    // ...
}
```

## Posix 互換デスクトップアプリケーション: MacOS, Linux, Windows

JVM 互換エンジンの代わりに、 バックエンドとして `curl` を利用した [Kotlin/Native](https://github.com/JetBrains/kotlin-native) で作成された Ktor クライアントを利用することもできます。

```kotlin
dependencies {
    implementation("io.ktor:ktor-client-curl:$ktor_version")
    // ...
}
```

## サンプル

Ktor サンプルリポジトリ [mpp/client-mpp](https://github.com/ktorio/ktor-samples/tree/master/mpp/client-mpp) に、共通クライアントを使用したフル実装のサンプルがあります。

このプロジェクトを参考に作成することができます。
このプロジェクトでは実験的な Gradle タスクもいくつか公開されており、 Gradle タスクから直接 Android や iOS アプリケーションをビルド、インストール、実行することができます。

### Android タスク

* `:client-mpp-android:emulatorList` - 利用可能なエミュレータの一覧を表示
* `:client-mpp-android:emulatorStart` - エミュレータを起動
    * 現時点では Gradle と同期的に起動してしまうため、ターミナルを別で起動することをおすすめします
* `:client-mpp-android:emulatorInstall` - エミュレータにアプリケーションをインストール
* `:client-mpp-android:emulatorRun` - エミュレータ内のアプリケーションを起動

### iOS タスク

* `:client-mpp-ios:startSimulator` - シミュレータを起動
* `:client-mpp-ios:shutdownSimulator` - シミュレータを停止
* `:client-mpp-ios:buildXcode` - Kolin/Native で作成された `.framework` を用いた XCode プロジェクトをビルド
* `:client-mpp-ios:installSimulator` - シミュレータにアプリケーションをインストール
* `:client-mpp-ios:launchSimulator` - シミュレータ内のアプリケーションを起動

これらのタスクは実験的なものであるため、特定の環境では失敗する可能性があります。
遭遇した際は、改善のためにご連絡ください。
あるいは、開発にご協力ください。

* [Android タスク](https://github.com/ktorio/ktor-samples/blob/master/mpp/client-mpp/android/build.gradle){:target="_blank"}
* [iOS タスク](https://github.com/ktorio/ktor-samples/blob/master/mpp/client-mpp/ios/build.gradle){:target="_blank"}
