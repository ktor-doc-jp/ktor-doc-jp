---
title: Docker
caption: Dockerコンテナの作成
category: quickstart
permalink: /quickstart/quickstart/docker.html
redirect_from:
  - /quickstart/docker.html
---

[Docker](https://www.docker.com)はコンテナプラットフォームです。
それを使うことで任意のオペレーティングシステムで独立した形で動くようソフトウェアのパッケージングをすることができます。

KtorアプリケーションをDockerで配信することは非常に簡単で、数ステップでできます。

* [Docker](https://www.docker.com)のインストール
* JARパッケージングツール

このページではDockerイメージの作成とイメージへのアプリケーションの設置方法について説明します。

**目次:**

* TOC
{:toc}

## Gradleを使ったアプリケーションのパッケージング

このチュートリアルでは、Gradle [shadow plugin](https://github.com/johnrengelman/shadow)を使います。
それを使うことで、全Outputクラス・resource・必要な依存ライブラリを１つのJarファイルにパッケージングでき、
またmainメソッドを含むエントリーポイントmainクラスがどれかをJavaに知らせるマニフェストファイルも添付することができます。

初めに、shadow pluginへの依存を、`build.gradle`ファイルに追加する必要があります。

```groovy 
buildscript {
    ...
    repositories {
        ...
        maven { url "https://plugins.gradle.org/m2/" }
    }
    dependencies {
        ...
        classpath "com.github.jengelman.gradle.plugins:shadow:2.0.1"
    }
}
```

その後、applicationプラグインと一緒にapplyする必要があります。

```groovy
apply plugin: "com.github.johnrengelman.shadow"
apply plugin: 'application'
``` 

Docker内部でJavaのJARが起動する際にどれを起動すればよいのか伝えるため、mainクラスを指定します。

```groovy
mainClassName = 'org.sample.ApplicationKt'
```

設定する値はmain関数を含むクラスの完全修飾名です。
`main`関数がファイルのトップレベル関数だったときは、クラス名はファイル名に`Kt`サフィックスを付与したものになります。
上の例だと、`main`関数は`org.sample`パッケージの`Application.kt`ファイルになります。

最後に、shadow pluginを設定する必要があります。

```groovy
shadowJar {
    baseName = 'my-application'
    classifier = null
    version = null
}
```

今、`./gradlew build`を実行することで、アプリケーションをビルド・パッケージングすることができます。
`build/libs`フォルダに`my-application.jar`ができるかと思います。

さらなる情報としては、[プラグインの設定方法のドキュメント](http://imperceptiblethoughts.com/shadow/)を御覧ください。

完全な`build.gradle`は例えば以下のようになります。

{% capture build-gradle %}
```groovy
buildscript {
    ext.kotlin_version = '{{site.kotlin_version}}'
    ext.ktor_version = '{{site.ktor_version}}'
    ext.logback_version = '1.2.3'
    ext.slf4j_version = '1.7.25'
    repositories {
        jcenter()
        maven { url "https://plugins.gradle.org/m2/" }
    }
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        classpath "com.github.jengelman.gradle.plugins:shadow:2.0.1"
    }
}

apply plugin: 'kotlin'
apply plugin: "com.github.johnrengelman.shadow"
apply plugin: 'application'

mainClassName = "io.ktor.server.netty.EngineMain"

sourceSets {
    main.kotlin.srcDirs = [ 'src' ]
    main.resources.srcDirs = [ 'resources' ]
}

repositories {
    jcenter()
}

dependencies {
    compile "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
    compile "io.ktor:ktor-server-netty:$ktor_version"
    compile "io.ktor:ktor-html-builder:$ktor_version"
    compile "ch.qos.logback:logback-classic:$logback_version"
}

kotlin.experimental.coroutines = 'enable'

shadowJar {
    baseName = 'my-application'
    classifier = null
    version = null
}
```
{% endcapture %}

{% capture resources-application-conf %}
```groovy
ktor {
    deployment {
        port = 8080
    }

    application {
        modules = [ io.ktor.samples.hello.HelloApplicationKt.main ]
    }
}
```
{% endcapture %}

{% capture src-hello-application-kt %}
```kotlin
package io.ktor.samples.hello

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.routing.*
import kotlinx.html.*

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    routing {
        get("/") {
            call.respondHtml {
                head {
                    title { +"Ktor: jetty" }
                }
                body {
                    p {
                        +"Hello from Ktor Jetty engine sample application"
                    }
                }
            }
        }
    }
}
```
{% endcapture %}

{% include tabbed-code.html
    tab1-title="build.gradle" tab1-content=build-gradle
    tab2-title="resources/application.conf" tab2-content=resources-application-conf
    tab3-title="src/HelloApplication.kt" tab3-content=src-hello-application-kt
%}


[動作例](https://github.com/ktorio/ktor-samples/tree/master/deployment/docker)をktor-samplesリポジトリで確認することもできます。
{: .note }

## Dockerイメージの準備

プロジェクトのrootフォルダ内で、`Dockerfile`という名前のファイルを以下の内容で作成してください。

{% capture dockerfile %}{% include docker-sample.md %}{% endcapture %}
{% include tabbed-code.html
    tab1-title="Dockerfile" tab1-content=dockerfile
    no-height="true"
%}

これが何なのか見てみましょう。

```dockerfile
FROM openjdk:8-jre-alpine
```

この行はDockerに対し、pre-builtイメージである[Alpine Linux](https://alpinelinux.org/)をベースにすることを伝えています。
もちろん他のイメージを[OpenJDK registry](https://hub.docker.com/_/openjdk/)から利用することもできます。
Alpine Linuxのメリットはイメージがとても小さいことです。

また、イメージ内のコードをコンパイルする必要がなくコンパイル済みクラスの実行のみしか行わない場合、JREのみのイメージを選択するといったこともできます。

```dockerfile
RUN mkdir /app
COPY ./build/libs/my-application.jar /app/my-application.jar
WORKDIR /app
```

これらの行はパッケージされたアプリケーションをDockerイメージ内にコピーし、コピー先ディレクトリをWorkingディレクトリとして指定しています。

```dockerfile
CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "my-application.jar"]
```

最終行は、Dockerに`java`をオプション・パッケージしたアプリケーション指定で起動するように指定しています。

## Dockerイメージのビルド・起動方法

アプリケーションパッケージのビルド:

```bash
./gradlew build
```

ビルドとイメージへのタグ付け:

```bash
docker build -t my-application .
```

イメージの起動:

```bash
docker run -m512M --cpus 2 -it -p 8080:8080 --rm my-application
```

このコマンドでDockerをforegroundモードで実行できます。
サーバーが終了するのを待ち、また`Ctrl+C`を押すことでも終了することができるモードです。
`-it`オプションを使うことでDockerにターミナル(*tty*)を割り当て、stdoutをパイプし、キー割り込みに反応するようにすることができます。

サーバーが独立したコンテナ内で今動いているため、Dockerに実際にサーバーにアクセスするためのportを開けるように指定する必要があります。
パラメーター`-p 8080:8080`はDockerにコンテナ内部のport番号8080を、ローカルマシンのport番号8080番として利用可能にするよう指定します。
それゆえに`localhost:8080`にブラウザ経由でアクセスした際、最初にDockerに到達した上でアプリケーションの`8080`番ポートにブリッジされます。

`-m512M`オプションでメモリを、`--cpus 2`オプションでCPU数を割当てできます。

デフォルトでは、コンテナのファイルシステムはコンテナが終了後も残るようになっていますが、`--rm`オプションをつけることでゴミファイルが残ることを防げます。

Dockerイメージの起動についてのさらなる情報は[docker runに関するドキュメント](https://docs.docker.com/engine/reference/run)をご覧ください。

## DockerイメージのPush

アプリケーションがローカルで無事起動したならば、次はそれをデプロイしてみましょう。

```bash
docker tag my-application hub.example.com/docker/registry/tag
docker push hub.example.com/docker/registry/tag
```

これらのコマンドはアプリケーションにタグ付けと、ImageのPushをしています。 
もちろん、`hub.example.com/docker/registry/tag`をあなたのレジストリの実際のURLと置き換えることもできます。

ここでは詳細に触れませんが、認証や特定のオプションや特定のツールに関する設定が必要とされることもあります。
あなたの組織やクラウドプラットフォームの情報を探してみたり、[docker pushに関するドキュメント](https://docs.docker.com/engine/reference/commandline/push/)を確認してみてください。

## サンプル

[動作サンプル](https://github.com/ktorio/ktor-samples/tree/master/deployment/docker)はktor-samplesリポジトリで確認できます。

