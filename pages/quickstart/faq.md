---
title: FAQ
caption: Frequently Asked Questions 
section: Quick Start
permalink: /quickstart/faq.html
priority: 100
redirect_from:
  - /servers/faq.html
---

In this section we provide answers to the frequent questions you ask us.

Can't find an answer for your question? Head over our #ktor [Kotlin Slack](http://slack.kotlinlang.org/){: target="_blank" rel="noopener"} channel,
and we will try to help you!
{: .note}

**Table of contents:**

* TOC
{:toc}

## What is the proper way to pronounce ktor?
{: #pronounce }

> `kay-tor`

## Ktor imports are not being resolved. Imports are in red.
{: #ktor-artifact }

> Ensure that you are including the ktor artifact. For example, with gradle and Netty engine would be:
> ```kotlin
> dependencies {
>     compile "io.ktor:ktor-server-netty:$ktor_version"
> }
> ```
> * For gradle, check: <http://ktor.io/quickstart/gradle.html#choose-your-engine-and-configure-it>
> * For maven, check: <http://ktor.io/quickstart/maven.html>

## Does ktor provide a way to catch ipc signals (e.g. SIGTERM or SIGINT) so server shutdown can be handled gracefully?
{: #sigterm }

> If you are running a `DevelopmentEngine`, it will be handled automatically.
>
> Otherwise you will have to [handle it manually](https://github.com/ktorio/ktor/blob/80f8c7bf352ac8075b8922b7f1aa94d7dc2ffdce/ktor-server/ktor-server-cio/src/io/ktor/server/cio/DevelopmentEngine.kt#L12).
> You can use `Runtime.getRuntime().addShutdownHook` JVM's facility.

## How do I get the client IP behind a proxy?
{: #proxy-ip }

> The property `call.request.origin` gives connection information about the original caller (the proxy)
> if the proxy provides proper headers and the feature `XForwardedHeadersSupport` is installed.

## I get the error 'java.lang.IllegalStateException: No instance for key AttributeKey: Locations'
{: #no-attribute-key-locations }

> You get this error if you try to use the locations feature without actually installing it. Check the locations feature:
> <https://ktor.io/features/locations.html>

## I get a 406 error with a client not sending an Accept header. With WRK I'm getting Non-2xx responses after adding JSON support
{: #missing-accept-issue }

> There is a [known issue](https://github.com/ktorio/ktor/issues/38) in Ktor <= 0.9.1,
> that when a client do not send Accept header, the content negotiation assumes no accepting anything.
> Since 0.9.2-alpha-1, ktor assumes it should accept everything when no Accept header is sent.

## How can I test latest commits on master?
{: #bleeding-edge }

You can use jitpack to get builds from master that are not yet released:
<https://jitpack.io/#ktorio/ktor>
Also you can [build ktor from source](/advanced/building-from-source.html), and use your mavenLocal repository for the artifact.  

## How can I be sure of which version of ktor am I using?
{: #ktor-version-used }

You can use the [`DefaultHeaders` feature](/features/default-headers.html) that will send a
Server header with the ktor version on it.
Something like this should be sent as part of the response headers: `Server: ktor-server-core/0.9.2-alpha-3 ktor-server-core/0.9.2-alpha-3` 

## Website accessibility tips and tricks
{: #website-tricks }

> You can use the keys `'s'` (search), `'t'` (github file finder flavor) or `'#'` to access the search in any page
> of the documentation website.
> The `'#'` version will limit the search to the sections in the current page.

> In the search you can either select the options with the mouse or fingers, or using the keyboard arrows `'↑'` `'↓'`
> and the return key `⏎` to go to the currently selected page.

> This search only uses page titles, and keywords for the search. But it also enables a google search
> in the `ktor.io` domain to do a full text search on all its contents. 

> Long code fragments that are folded, can be expanded by either clicking on
> the `'+'`/`'-'` symbol that appears on the top left corner always on mobile devices
> or on hover on devices with mouse.
> You can also double click the fragment to expand it.
> In addition to expanding it, this action selects the text so you can copy those fragments easily
> with `cmd+c` on mac, or `ctrl+c` in other operating systems.

> You can click on the headings and some notes, to get an anchored link to those sections.
> After clicking, you can copy the new url in your browsing including the `#` to link to a specific section.