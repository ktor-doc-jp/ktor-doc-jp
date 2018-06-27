---
title: FAQ
caption: Frequently Asked Questions 
category: quickstart
permalink: /quickstart/faq.html
priority: 100
redirect_from:
  - /servers/faq.html
---

In this section, we provide answers to the questions you frequently ask us.

Can't find an answer for your question? Head over our #ktor [Kotlin Slack](http://slack.kotlinlang.org/){: target="_blank" rel="noopener"} channel,
and we will try to help you!
{: .note}

**Table of contents:**

* TOC
{:toc}

## What is the proper way to pronounce ktor?
{: #pronounce }

> `kay-tor`

## What does CIO mean?
{: #cio }

CIO stands for Coroutine-based I/O. Usually we call it to an engine that uses Kotlin and Coroutines to implement
the logic implementing an IETF RFC or another protocol without relying on external JVM libraries.

## Ktor imports are not being resolved. Imports are in red.
{: #ktor-artifact }

> Ensure that you are including the ktor artifact. For example, for gradle and Netty engine it would be:
> ```kotlin
> dependencies {
>     compile "io.ktor:ktor-server-netty:$ktor_version"
> }
> ```
> * For gradle, check: <http://ktor.io/quickstart/gradle.html#engine>
> * For maven, check: <http://ktor.io/quickstart/maven.html>

## Does ktor provide a way to catch ipc signals (e.g. SIGTERM or SIGINT) so the server shutdown can be handled gracefully?
{: #sigterm }

> If you are running a `DevelopmentEngine`, it will be handled automatically.
>
> Otherwise you will have to [handle it manually](https://github.com/ktorio/ktor/blob/80f8c7bf352ac8075b8922b7f1aa94d7dc2ffdce/ktor-server/ktor-server-cio/src/io/ktor/server/cio/DevelopmentEngine.kt#L12).
> You can use `Runtime.getRuntime().addShutdownHook` JVM's facility.

## How do I get the client IP behind a proxy?
{: #proxy-ip }

> The property `call.request.origin` gives connection information about the original caller (the proxy)
> if the proxy provides proper headers, and the feature `XForwardedHeadersSupport` is installed.

## I get the error 'java.lang.IllegalStateException: No instance for key AttributeKey: Locations'
{: #no-attribute-key-locations }

> You get this error if you try to use the locations feature without actually installing it. Check the locations feature:
> <https://ktor.io/features/locations.html>

## I get a 406 error with a client not sending an Accept header. With WRK I'm getting Non-2xx responses after adding JSON support
{: #missing-accept-issue }

> There is a [known issue](https://github.com/ktorio/ktor/issues/38) in Ktor <= 0.9.1,
> that when a client does not send an Accept header, the content negotiation assumes that there is no accepting anything.
> Since 0.9.2-alpha-1, ktor assumes it should accept everything when no Accept header is sent.

## How can I test the latest commits on master?
{: #bleeding-edge }

You can use jitpack to get builds from master that are not yet released:
<https://jitpack.io/#ktorio/ktor>
Also you can [build ktor from source](/advanced/building-from-source.html), and use your mavenLocal repository for the artifact.  

## How can I be sure of which version of ktor am I using?
{: #ktor-version-used }

You can use the [`DefaultHeaders` feature](/features/default-headers.html) that will send a
Server header with the ktor version in it.
Something like this should be sent as part of the response headers: `Server: ktor-server-core/0.9.2-alpha-3 ktor-server-core/0.9.2-alpha-3` 

## Website accessibility tips and tricks
{: #website-tricks }

> You can use the keys <kbd>s</kbd> (search), <kbd>t</kbd> (github file finder flavor) or <kbd>#</kbd> to access the search in any page
> of the documentation website.
> The <kbd>#</kbd> version will limit the search to the sections in the current page.

> In the search you can either select the options with the mouse or fingers, or using the keyboard arrows <kbd>↑</kbd> <kbd>↓</kbd>
> and the return key <kbd>⏎</kbd> to go to the currently selected page.

> This search only uses page titles, and keywords for the search. It is also possible to do a google search
> in the `ktor.io` domain to do a full text search on all its contents. 

> Long code fragments that are folded, can be expanded by either clicking on
> the `'+'`/`'-'` symbol that always appears in the top left corner of mobile devices
> or on hover on devices with mouse.
> You can also double click the fragment to expand it.
> In addition to expanding it, this action selects the text so you can copy the fragments easily
> with <kbd>cmd</kbd> + <kbd>c</kbd> on mac, or <kbd>ctrl</kbd> + <kbd>c</kbd> in other operating systems.

> You can click on the headings and some notes, to get an anchored link to the sections.
> After clicking, you can copy the new url in your browser including the `#` to link to a specific section.

## My route is not being executed, how can I debug it?
{: #route-not-executing }

Ktor provides a tracing mechanism for the routing feature to help troubleshooting
routing decisions. Check the [Tracing the routing decisions](/features/routing.html#tracing) section in the Routing page.

## I get a `io.ktor.pipeline.InvalidPhaseException: Phase Phase('YourPhase') was not registered for this pipeline`.
{: #invalid-phase }

This means that you are trying to use a phase that is not registered as a reference for another phase.
This might happen for example in the Routing feature if you try to register a phase relation inside a node,
but the phase referenced is defined in another ancestor Route node. Since route phases and interceptors are later
merged, it should work, but you need to register it in your Route node:

```kotlin
route.addPhase(PhaseDefinedInAncestor)
route.insertPhaseAfter(PhaseDefinedInAncestor, MyNodePhase)
```

## How can I subscribe to ktor events?
{: #ktor-events }

There is a page [explaining the Ktor's application-level event system](/advanced/events.html). 
