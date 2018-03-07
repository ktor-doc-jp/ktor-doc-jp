---
title: FAQ
caption: Frequently Asked Questions 
section: Servers
permalink: /servers/faq.html
---

In this section we provide answers to the frequent questions you ask us.

Can't find an answer for your question? Head over our #ktor [Kotlin Slack](http://slack.kotlinlang.org/){: target="_blank"} channel,
and we will try to help you!
{: .note}

**Table of contents:**

* TOC
{:toc}

#### Ktor imports are not being resolved. Imports are in red.
{: #ktor-artifact }

Ensure that you are including the ktor artifact. For example, with gradle and Netty engine would be:
```kotlin
dependencies {
    compile "io.ktor:ktor-server-netty:$ktor_version"
}
```
* For gradle, check: <http://ktor.io/quickstart/gradle.html#choose-your-engine-and-configure-it>
* For maven, check: <http://ktor.io/quickstart/maven.html>

#### Does ktor provide a way to catch ipc signals (e.g. SIGTERM or SIGINT) so server shutdown can be handled gracefully?
{: #sigterm }

If you are running a `DevelopmentEngine`, it will be handled automatically.

Otherwise you will have to [handle it manually](https://github.com/ktorio/ktor/blob/80f8c7bf352ac8075b8922b7f1aa94d7dc2ffdce/ktor-server/ktor-server-cio/src/io/ktor/server/cio/DevelopmentEngine.kt#L12).
You can use `Runtime.getRuntime().addShutdownHook` JVM's facility.

#### How do I get the client IP behind a proxy?
{: #proxy-ip }

The property `call.request.origin` gives connection information about the original caller (the proxy)
if the proxy provides proper headers and the feature `XForwardedHeadersSupport` is installed.

