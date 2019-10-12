---
title: Gradle
caption: Gradleビルドの設定
category: quickstart
toc: true
permalink: /quickstart/quickstart/gradle.html
redirect_from:
  - /quickstart/gradle.html
  - /quickstart/quickstart/intellij-idea/gradle.html
  - /quickstart/quickstart/intellij-idea/plugin.html
---

このガイドでは`build.gradle`ファイルの作成方法とKtorをサポートするための設定方法について説明します。

**目次:**

* TOC
{:toc}

## 基本的なKotlinの`build.gradle`ファイル(Ktor設定無し)
{: #initial }

まず初めに、Kotlinを含むスケルトンの`build.gradle`ファイルが必要です。
任意のテキストエディタで作成できますし、IntelliJを使う場合は[IntelliJ guide](/quickstart/quickstart/intellij-idea.html)の方法で作成できます。

最初のファイルは以下のような見た目になります:

{% capture build-gradle %}
```groovy
group 'Example'
version '1.0-SNAPSHOT'

buildscript {
    ext.kotlin_version = '{{site.kotlin_version}}'

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}

apply plugin: 'java'
apply plugin: 'kotlin'

sourceCompatibility = 1.8

repositories {
    jcenter()
}

dependencies {
    compile "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
    testCompile group: 'junit', name: 'junit', version: '4.12'
}
```
{% endcapture %}

{% include tabbed-code.html
    tab1-title="build.gradle" tab1-content=build-gradle
%}

## Ktorへの依存の追加とビルド設定
{: #ktor-dependencies}

Ktorの成果物はbintrayのレポジトリに配置されています。
そしてそのコアの部分は`kotlinx.coroutines`ライブラリに依存しており、それは`jcenter`リポジトリ内にあります。

そのため両方のリポジトリを`build.gradle`ファイルの`repositories`ブロックに追加する必要があります:

```groovy
jcenter()
```

Ktorの成果物の参照ごとにバージョンを指定する必要があります。
重複を避けるため、`buildscript`ブロック内のextraプロパティ（または`gradle.properties`ファイル内）にバージョンを指定するとそれを後で使うことができます。

```groovy
ext.ktor_version = '{{site.ktor_version}}'
```

`ktor-server-core`を設定したバージョン`ktor_version`で追加する必要があります。

```groovy
compile "io.ktor:ktor-server-core:$ktor_version"
```

groovyにおいては、シングルクオートの文字列とダブルクオートの文字列があり、
ダブルクオートの文字列においては上のバージョン値のような変数の展開ができます。
そのためダブルクオートの文字列を使う必要があります。
{: .note.tip }

KotlinコンパイラーにJava8互換のバイトコードの生成をすることを伝える必要があります。
{: #java8}

```groovy
compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
```

## エンジンの選択と設定
{: #engine}

Ktorは様々な環境で動作します。
例えばNetty、Jetty、その他Servlet互換のアプリケーションコンテナ（例：Tomcat）などです。

以下の例はNettyベースでKtorを設定する方法です。
その他のエンジンについては[成果物](/quickstart/artifacts.html)をご覧ください。

`ktor-server-netty`への依存を追加し、前に定義した`ktor_version`プロパティを指定します。
このモジュールはNettyによるWebサーバと、その上でKtorが動く上で必要なコードを提供します。

```groovy
compile "io.ktor:ktor-server-netty:$ktor_version"
```

## 最終的な`build.gradle`ファイル(Ktor設定有り)
{: #complete}

設定が完了すれば、`build.gradle`ファイルは以下のようになっているかと思います:

{% capture build-gradle %}
```groovy
group 'Example'
version '1.0-SNAPSHOT'

buildscript {
    ext.kotlin_version = '{{site.kotlin_version}}'
    ext.ktor_version = '{{site.ktor_version}}'

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}

apply plugin: 'java'
apply plugin: 'kotlin'

sourceCompatibility = 1.8
compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
}

kotlin {
    experimental {
        coroutines "enable"
    }
}

repositories {
    jcenter()
}

dependencies {
    compile "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
    compile "io.ktor:ktor-server-netty:$ktor_version"
    testCompile group: 'junit', name: 'junit', version: '4.12'
}
```
{% endcapture %}

{% include tabbed-code.html
    tab1-title="build.gradle" tab1-content=build-gradle
%}

Gradleを起動することで依存の解決と設定の妥当性検証を行うことができます。（起動は`gradle`コマンドか、gradle wrapperを利用する場合は`./gradlew`）

以下のチュートリアルでは最も基本的な設定から、アプリケーション開発を始める際に使える機能を盛り込んだ設定までお見せします。

## IntelliJ: 事前要件

1.  最新バージョンのIntelliJ IDEAがインストールされていること
2.  Kotlinプラグイン、Gradleプラグインが有効になっていること(デフォルトでは有効です)

IntelliJ IDEAのメインメニューで以下の場所からチェックできます。
* Windows: `File -> Settings -> Plugins`
* Mac: `IntelliJ IDEA -> Settings -> Plugins`

## IntelliJ: プロジェクトの開始

1.  `File -> New -> Project`:

    ![Ktor IntelliJ: File New Project](/quickstart/intellij-idea/file-new-project.png)

2.  `Gradle`を選択し、`Additional Libraries and Frameworks`の下にある`Java`と`Kotlin (Java)`をチェックします。プロジェクトのSDKを確認し`Next`をクリックします:

    ![Ktor IntelliJ: Gradle Kotlin JVM](/quickstart/intellij-idea/gradle-kotlin-jvm.png)

3.  GroupIdを入れます: `Example`
    ArtifactIdを入れます: `Example`
    Nextをクリックします:

    ![Ktor IntelliJ: GroupId](/quickstart/intellij-idea/groupid.png)

4.  Project名を入れます: `Example`
    Projectのファイルパスを指定します: `a/path/on/your/filesystem`
    `Finish`をクリックします:

    ![Ktor IntelliJ: Project Location Name](/quickstart/intellij-idea/project-location-name.png)

5.  Gradleが動き出すまで数秒待ちます。そうすると以下のようにプロジェクト構造が見れるようになります。:

    ![Ktor IntelliJ: Project Structure](/quickstart/intellij-idea/project-structure.png)

6.  `build.gradle`ファイルの成果物やリポジトリ設定を更新します:
    * `compile("io.ktor:ktor-server-netty:$ktor_version")`を`build.gradle`の`dependencies`ブロックに追加
    * `jcenter()`を`repositories`ブロックに追加

    ![Ktor IntelliJ: Build Gradle](/quickstart/intellij-idea/build-gradle.png)

`build.gradle`の設定に関する詳細な説明は[Gradleビルドの設定](/quickstart/quickstart/gradle.html)をご覧ください。

Auto Importオプションを設定したい場合、右下のほうにAuto Importの設定を設定を促す通知が出てくるのでクリックすることで設定を行うことができます。 
{: .note}

## IntelliJ: Gradleのセットアップ

このセクションはGradleについての基本的な知識を持っている人を想定しています。
Gradleをもし使ったことがなければ、gradle.orgがGradleを始める上で有効な[いくつかのガイド](https://guides.gradle.org/building-java-applications/)を提供してくれています。
{: .note}

単純なKtorアプリケーションを以下のようにしてセットアップすることができます。

![Ktor Build with Gradle](/quickstart/1/ktor_build_gradle.png)

{% capture gradle-kotlin-build %}
```kotlin
// build.gradle.kts

import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "Example"
version = "1.0-SNAPSHOT"

val ktor_version = "{{ site.ktor_version }}"

plugins {
    application
    kotlin("jvm") version "{{ site.kotlin_version }}"
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClassName = "MainKt"
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("io.ktor:ktor-server-netty:$ktor_version")
    compile("ch.qos.logback:logback-classic:1.2.3")
    testCompile(group = "junit", name = "junit", version = "4.12")
}
```
{% endcapture %}

{% capture gradle-groovy-build %}
```groovy
// build.gradle

group 'Example'
version '1.0-SNAPSHOT'

buildscript {
    ext.kotlin_version = '{{ site.kotlin_version }}'
    ext.ktor_version = '{{ site.ktor_version }}'

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}

apply plugin: 'java'
apply plugin: 'kotlin'
apply plugin: 'application'

mainClassName = 'MainKt'

sourceCompatibility = 1.8
compileKotlin { kotlinOptions.jvmTarget = "1.8" }
compileTestKotlin { kotlinOptions.jvmTarget = "1.8" }

repositories {
    mavenCentral()
}

dependencies {
    compile "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
    compile "io.ktor:ktor-server-netty:$ktor_version"
    compile "ch.qos.logback:logback-classic:1.2.3"
    testCompile group: 'junit', name: 'junit', version: '4.12'
}
```
{% endcapture %}

Text version:
{% include gradle.html gradle-kotlin=gradle-kotlin-build gradle-groovy=gradle-groovy-build %}

Ktorが1.0になる前、カスタムのMavenリポジトリをearly previewの成果物を配布するために用意していました。
以下に示すようないくつかのリポジトリを指定することでそれらを参照することができるようになります。

もちろん、実際の成果物を含めることも忘れてはいけません。
Quickstartのページでは、`ktor-server-netty`を使いました。
その成果物はKtorのコア部分、Netty、KtorとNettyとのコネクタを推移的依存として含んでいます。
もちろんその他の依存を好きに追加することができあｍす。

Ktorはモジュール化した設計になっているため、追加で他のリポジトリにある成果物から特定の機能を追加することもできます。
必要な成果物（およびそれを配布するリポジトリ）を機能ごとに探す必要があります。
{:.note}

## IntelliJ: アプリケーションの作成

`src/main/kotlin`ディレクトリを選択し、新しいパッケージを作成します。
新しいパッケージ名は`blog`にします。

ディレクトリを選択し、その下に`BlogApp`という名前で新しいファイルを作成します。

![Ktor IntelliJ: Create Kotlin File](/quickstart/intellij-idea/create-kotlin-file.png)

![Ktor IntelliJ: Create Kotlin File Name](/quickstart/intellij-idea/create-kotlin-file-name.png)

以下のコードをコピーアンドペーストします。

{% capture blog-app %}
```kotlin
package blog

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080) {
        routing {
            get("/") {
                call.respondText("My Example Blog", ContentType.Text.Html)
            }
        }
    }.start(wait = true)
}
```
{% endcapture %}

{% include tabbed-code.html
    tab1-title="BlogApp.kt" tab1-content=blog-app
    no-height="true"
%}

![Ktor IntelliJ: Program](/quickstart/intellij-idea/program.png)

これで'`blog.BlogAppKt`'を起動できるようになりました。
the **🐞**{: style="transform:rotate(90deg);display:inline-block;"}がついているアイコンを押し、`Debug 'blog.BlogAppKt'`を選択します。

![Ktor IntelliJ: Program Run](/quickstart/intellij-idea/program-run.png)

これによってIntelliJの右上部に起動設定も作成され、再度同じ設定で簡単に実行できるようになります。

![Ktor IntelliJ: Program Run Config](/quickstart/intellij-idea/program-run-config.png)

これによりNettyによるWebサーバが起動します。
ブラウザでURL`localhost:8080`を入力することでブログサンプルページを見ることができるようになります。

![Ktor IntelliJ: Website](/quickstart/intellij-idea/website.png)

## IntelliJ: アプリケーションオブジェクトを使うことで改善

上で行ったせて地はたくさんのネストブロックが発生するため、アプリケーションに機能を追加していく上で理想的ではない状況です。
そこでApplicationオブジェクトを使いそれをmain関数内のembeddedServerから参照することで、その点を改善することができます。

BlogApp.kt内のコードを以下のように変更し試してみてください:

{% capture blog-app %}
```kotlin
package blog

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)
    install(Routing) {
        get("/") {
            call.respondText("My Example Blog2", ContentType.Text.Html)
        }
    }
}

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080, watchPaths = listOf("BlogAppKt"), module = Application::module).start()
}
```
{% endcapture %}

{% include tabbed-code.html
    tab1-title="BlogApp.kt" tab1-content=blog-app
    no-height="true"
%}

## IntelliJ: 設定データの切り出し

上のコードではmain関数のembeddedServer内でアプリケーション設定を行うようになっていますが、
別の設定ファイルに切り出すことで将来的なデプロイや変更に対しより柔軟性をもたせることができるようになります。
`src/main/resources`ディレクトリ内において、`application.conf`という名前で以下の内容のテキストファイルを新規作成します:

{% capture application-conf %}
```kotlin
ktor {
    deployment {
        port = 8080
    }

    application {
        modules = [ blog.BlogAppKt.main ]
    }
}
```
{% endcapture %}

{% include tabbed-code.html
    tab1-title="application.conf" tab1-content=application-conf
    no-height="true"
%}

その後`BlogApp.kt`からmain関数を削除し、`fun Application.module()`を`fun Application.main()`に変更します。
しかし、今アプリケーションを起動しても、"Top-level function 'main' not found in package blog." というエラーメッセージとともに失敗します。
`Application.main()`関数は今拡張関数でありトップレベルのmain関数として認識できないからです。

IntelliJ IDEAは自動的に認識はできないので、新しいmainクラスを指定してやる必要があります。
`build.gradle`に以下のように追加します:

{% capture gradle-groovy-build %}
```groovy
// build.gradle

apply plugin: 'application'

//mainClassName = 'io.ktor.server.netty.DevelopmentEngine' // For versions < 1.0.0-beta-3
mainClassName = 'io.ktor.server.netty.EngineMain' // Starting with 1.0.0-beta-3
```
{% endcapture %}

{% capture gradle-kotlin-build %}
```kotlin
// build.gradle.kts

plugins {
    application
    // ...
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}
```
{% endcapture %}

{% include gradle.html gradle-kotlin=gradle-kotlin-build gradle-groovy=gradle-groovy-build %}

その後`Run -> Edit Configurations`を開き、`blog.BlogAppKt`の設定を選択し、mainクラスを`io.ktor.server.netty.EngineMain`に変更します。

新しい設定で起動すると、アプリケーションが再び起動できるようになります。

## ログ設定
{: #logging}

アプリケーションイベントやその他有益な情報をログ出力したい場合は、[ロギング](/servers/logging.html)ページをご覧ください。

