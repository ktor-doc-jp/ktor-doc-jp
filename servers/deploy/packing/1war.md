---
title: WAR
caption: WAR (サーブレットコンテナ)
category: servers
permalink: /servers/deploy/packing/war.html
ktor_version_review: 1.0.0
---

WAR アーカイブを利用すると、 `webapps` ディレクトリに war ファイルを配置するだけで、ウェブコンテナ / サーブレットコンテナ上で動作する
アプリケーションを簡単にデプロイできるようになります。
さらに、 [Google App Engine](https://cloud.google.com/appengine/) へのデプロイも可能です。

war ファイルを作成するために、 Gradle プラグイン gretty を利用します。
また、下記のような `WEB-INF/web.xml` が必要になります。

{% capture web-xml %}
```xml
<?xml version="1.0" encoding="ISO-8859-1" ?>

<web-app xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
         version="3.0">
    <!-- (必須) application.conf のパスの指定 -->
    <!-- クラスパスからの絶対パスとして読み込まれることに注意してください  -->
    <context-param>
        <param-name>io.ktor.ktor.config</param-name>
        <param-value>application.conf</param-value>
    </context-param>
	
    <servlet>
        <display-name>KtorServlet</display-name>
        <servlet-name>KtorServlet</servlet-name>
        <servlet-class>io.ktor.server.servlet.ServletApplicationEngine</servlet-class>

        <!-- 必須! -->
        <async-supported>true</async-supported>

        <!-- (任意) 最大100MB のファイルのアップロード-->
        <multipart-config>
            <max-file-size>304857600</max-file-size>
            <max-request-size>304857600</max-request-size>
            <file-size-threshold>0</file-size-threshold>
        </multipart-config>
    </servlet>

    <servlet-mapping>
        <servlet-name>KtorServlet</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>

</web-app>
```
{% endcapture %}

{% capture build-gradle %}
```groovy
buildscript {
    ext.gretty_version = '2.0.0'

    repositories {
        jcenter()
    }
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        classpath "org.akhikhl.gretty:gretty:$gretty_version"
    }
}

apply plugin: 'kotlin'
apply plugin: 'war'
apply plugin: 'org.akhikhl.gretty'

webAppDirName = 'webapp'

gretty {
    contextPath = '/'
    logbackConfigFile = 'resources/logback.xml'
}

sourceSets {
    main.kotlin.srcDirs = [ 'src' ]
    main.resources.srcDirs = [ 'resources' ]
}

repositories {
    jcenter()
}

dependencies {
    compile "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
    compile "io.ktor:ktor-server-servlet:$ktor_version"
    compile "io.ktor:ktor-html-builder:$ktor_version"
    compile "ch.qos.logback:logback-classic:$logback_version"
}

kotlin.experimental.coroutines = 'enable'

task run

afterEvaluate {
    run.dependsOn(tasks.findByName("appRun"))
}
```
{% endcapture %}

{% include tabbed-code.html
    tab1-title="webapp/WEB-INF/web.xml" tab1-content=web-xml
    tab2-title="build.gradle" tab2-content=build-gradle
%}

この Gradle ビルドスクリプトにはアプリケーションを起動するために必要な [タスク](http://akhikhl.github.io/gretty-doc/Gretty-tasks)
が定義されています。

WARファイルを生成したいだけの場合、 war プラグインで定義された `war` タスクを使用します。
`./gradlew war` を実行するだけで、 `/build/libs/projectname.war` が生成されます。
{: .note #generate-war-file }

コード全体 (例) : <https://github.com/ktorio/ktor-samples/tree/master/deployment/jetty-war>
{: .note.example}
