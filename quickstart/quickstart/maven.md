---
title: Maven
caption: Mavenビルドの設定
category: quickstart
toc: true
permalink: /quickstart/quickstart/maven.html
redirect_from:
  - /quickstart/maven.html
---

このガイドでは、Mavenの`pom.xml`ファイルの作成方法とKtorをサポートするための設定方法について説明します。

**目次:**

* TOC
{:toc}

## 基本的なKotlinの`pom.xml`ファイル(Ktor無し)
{: #initial }

Mavenは主にJavaプロジェクトに利用される自動ビルドツールです。
Mavenは`pom.xml`ファイルからプロジェクトの設定を読み取ります。
以下は基本的なKotlinアプリケーションビルド用の`pom.xml`ファイルです。

{% capture pom-xml %}
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>org.jetbrains</groupId>
    <artifactId>sample</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>org.jetbrains sample</name>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <kotlin.version>{{site.kotlin_version}}</kotlin.version>
        <junit.version>4.12</junit.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-stdlib</artifactId>
            <version>${kotlin.version}</version>
        </dependency>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-test-junit</artifactId>
            <version>${kotlin.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <sourceDirectory>src/main/kotlin</sourceDirectory>
        <testSourceDirectory>src/test/kotlin</testSourceDirectory>

        <plugins>
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
                <version>${kotlin.version}</version>
                <executions>
                    <execution>
                        <id>compile</id>
                        <phase>compile</phase>
                        <goals>
                            <goal>compile</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>test-compile</id>
                        <phase>test-compile</phase>
                        <goals>
                            <goal>test-compile</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```
{% endcapture %}

{% include tabbed-code.html
    tab1-title="pom.xml" tab1-content=pom-xml
%}

## Ktorへの依存追加とビルド設定
{: #ktor-dependencies}

Ktorの成果物はbintrayの特定のレポジトリに配置されています。
そしてその核となる部分は`jcenter`にある`kotlinx.coroutines`ライブラリに依存しています。

そのためその両方を`pom.xml`ファイルの`repositories`ブロックに追加してやる必要があります。

```xml
<repositories>
    <repository>
        <id>jcenter</id>
        <url>https://jcenter.bintray.com</url>
    </repository>
</repositories>
``` 

[Bintray](https://bintray.com/kotlin/ktor/ktor)サイトを見て、Ktorの最新バージョンを見つけてください。
この場合それは `{{site.ktor_version}}`です.

Ktorの成果物リファレンスからバージョンを指定する必要があり、重複を避けるためそのバージョンを`properties`ブロック内でextraプロパティとして指定する必要があります。

```xml
<properties>
    <ktor.version>{{site.ktor_version}}</ktor.version>
</properties>
```

`ktor-server-core`の成果物を追加し、あなたが決めた`ktor.version`を指定します:
 
```xml
<dependency>
    <groupId>io.ktor</groupId>
    <artifactId>ktor-server-core</artifactId>
    <version>${ktor.version}</version>
</dependency>
```

## エンジンの決定と設定
{: #engine}

Ktorは様々な環境で動作します。例えばNetty、Jetty、そのたServlet互換のあるアプリケーションコンテナ（例：Tomcat）などでです。

以下の例はKtorをNettyで動作させる例です。
その他のエンジンについては[artifacts](/quickstart/artifacts.html)をご覧ください。

`ktor-server-netty`への依存を追加し`ktor.version`プロパティを指定します。
このモジュールはNettyを提供し、Ktorアプリケーションがその上で動作するうえでWebサーバとして動作できるようにします。

```xml
<dependency>
    <groupId>io.ktor</groupId>
    <artifactId>ktor-server-netty</artifactId>
    <version>${ktor.version}</version>
</dependency>
```

## 最終的な`pom.xml`(Ktor有り)
{: #complete}

設定が終わったなら、`pom.xml`ファイルは以下のようになっているかと思います。

{% capture pom-xml %}
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>org.jetbrains</groupId>
    <artifactId>sample</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>org.jetbrains sample</name>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <kotlin.version>{{site.kotlin_version}}</kotlin.version>
        <ktor.version>{{site.ktor_version}}</ktor.version>
        <junit.version>4.12</junit.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-stdlib</artifactId>
            <version>${kotlin.version}</version>
        </dependency>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-test-junit</artifactId>
            <version>${kotlin.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>io.ktor</groupId>
            <artifactId>ktor-server-netty</artifactId>
            <version>${ktor.version}</version>
        </dependency>

    </dependencies>

    <build>
        <sourceDirectory>src/main/kotlin</sourceDirectory>
        <testSourceDirectory>src/test/kotlin</testSourceDirectory>

        <plugins>
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
                <version>${kotlin.version}</version>
                <executions>
                    <execution>
                        <id>compile</id>
                        <phase>compile</phase>
                        <goals>
                            <goal>compile</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>test-compile</id>
                        <phase>test-compile</phase>
                        <goals>
                            <goal>test-compile</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <jvmTarget>1.8</jvmTarget>
                    <args>
                        <arg>-Xcoroutines=enable</arg>
                    </args>
                </configuration>
            </plugin>
        </plugins>
    </build>

    <repositories>
        <repository>
            <id>jcenter</id>
            <url>http://jcenter.bintray.com</url>
        </repository>
    </repositories>
</project>
```
{% endcapture %}

{% include tabbed-code.html
    tab1-title="pom.xml" tab1-content=pom-xml
%}

`mvn package`コマンドを実行することで、依存ライブラリの解決や設定の妥当性検証を行うことができます。

## ログ設定
{: #logging}

アプリケーションイベントやその他有益な情報のログを残したい場合は、[logging](/servers/logging.html)ページをご覧ください。



