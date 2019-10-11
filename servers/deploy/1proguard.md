---
title: Proguard
caption: Proguard
category: servers
permalink: /servers/deploy/proguard.html
ktor_version_review: 1.0.0
---

JARのサイズに制限があるとき（例えば[heroku](/servers/deploy/hosting/heroku)に無料でアプリケーションをデプロイするとき）、
proguardを使ってサイズを小さくすることができます。
gradleを使っているなら、`proguard-gradle`を使うのがよいでしょう。
main moduleメソッド、EngineMainクラス、Kotlin reflectクラスを保持しておくことだけは覚えておく必要があります。
その他必要に応じて微調整することもできます。

{% capture build-gradle %}
```groovy
buildscript {
    ext.proguard_version = '6.0.1'
    dependencies {
        classpath "net.sf.proguard:proguard-gradle:$proguard_version"
    }
}

task minimizedJar(type: proguard.gradle.ProGuardTask, dependsOn: shadowJar) {
    injars "build/libs/my-application.jar"
    outjars "build/libs/my-application.min.jar"
    libraryjars System.properties.'java.home' + "/lib/rt.jar"
    printmapping "build/libs/my-application.map"
    ignorewarnings
    dontobfuscate
    dontoptimize
    dontwarn

    def keepClasses = [
            'io.ktor.server.netty.EngineMain', // The EngineMain you use, netty in this case.
            'kotlin.reflect.jvm.internal.**',
            'io.ktor.samples.hello.HelloApplicationKt', // The class containing your module defined in the application.conf
            'kotlin.text.RegexOption'
    ]

    for (keepClass in keepClasses) {
        keep access: 'public', name: keepClass, {
            method access: 'public'
            method access: 'private'
        }
    }
}
```
{% endcapture %}

{% include tabbed-code.html
    tab1-title="build.gradle" tab1-content=build-gradle
%}

&nbsp;

完全な例はこちらにあります: <https://github.com/ktorio/ktor-samples/tree/master/other/proguard> 
{: .note.example}
