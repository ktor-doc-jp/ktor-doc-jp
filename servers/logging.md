---
title: ロギング
caption: Ktorにおけるロギング
category: servers
permalink: /servers/logging.html
keywords: SLF4J logback log4j
---

Ktorは[SLF4J](https://www.slf4j.org/)をロギングに利用しています。

## SLF4Jプロバイダー
{: #providers }

ロギングプロバイダーを追加しない場合、アプリケーション起動時に以下のようなメッセージが出ます。

```
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
```

これらのwarningメッセージを取り除き、また何がアプリケーションで起きているかをより理解するため、
プロバイダーを追加することでロギングのセットアップをすることができます。

プロバイダーはJavaの[ServiceLoader](https://docs.oracle.com/javase/7/docs/api/java/util/ServiceLoader.html)メカニズムを使っています。
そのためコード内で何かしらをすることなしに、自動で発見され追加されます。
{: .note.tip }

### Logbackプロバイダー
{: #providers-logback }

SLF4Jプロバイダーとして、log4jの後継である[logback](https://logback.qos.ch/)を使うことができます。

Gradleの`build.gradle`または`build.gradle.kts`は以下のようになります:
```groovy
compile("ch.qos.logback:logback-classic:1.2.3")
```

Mavensの`pom.xml`:
```xml
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.2.3</version>
</dependency>
```

追加し、アプリケーションを起動すると、IntelliJ IDEAのRunページにてロギングメッセージを見ることができることができます。
しかし、これらのロギングメッセージはさらに有益にする余地が残っています。

### Logbackプロバイダーの設定
{: #providers-logback-config }

デフォルトのロギングで十分ではない場合、`logback.xml`か`logback-test.xml`（プライオリティはこちらのほうが高いです）ファイルを
`src/main/resources`フォルダに置き、ロギングの調整を行うことができます。
例えば:

{% capture logback-xml %}
```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{YYYY-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="trace">
        <appender-ref ref="STDOUT"/>
    </root>

    <logger name="org.eclipse.jetty" level="INFO"/>
    <logger name="io.netty" level="INFO"/>
</configuration>
```
{% endcapture %}

{% include tabbed-code.html
    tab1-title="logback.xml" tab1-content=logback-xml
    no-height="true"
%}

これを追加した後に、アプリケーションを停止し再度起動しブラウザでlocalhost:8080を見ると、
IDEAのrunページで以下のようなログメッセージを見ることができるはずです:

```
2017-05-29 23:08:12.926 [nettyCallPool-4-1] TRACE ktor.application - 200 OK: GET - /
```

リクエストのログを出力するには[Call Logging](/servers/features/call-logging.html) Featureをインストールすればよいです。
{: .note}

どのように`logback.xml`ファイルを変更しログ出力形式を変更すればよいかを理解するには、[logbackマニュアル](https://logback.qos.ch/manual/index.html)を見てください。

## メインロガーへのアクセス
{: #main-logger }

`ApplicationEnvironment`インターフェースは`log`プロパティを持っています。
`ApplicationCall`において`call.application.environment.log`を使ってアクセスすることができます。
