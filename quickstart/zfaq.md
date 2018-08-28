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

## How do I put questions/report bugs/contact you/contribute/give feedback, etc.?
{: #feedback }

Depending on the content, you might consider several channels:

* **GitHub:** Feature request, change suggestions/proposals, bugs and PRs.
* **Slack:** Questions, troubleshooting, guidance etc.
* **StackOverflow:** Questions.

**Rationale:**

For questions or troubleshooting we highly recommend you to use Slack or StackOverflow.

Think that using GitHub issues would notify all the people that is subscribed potentially with emails,
troubleshooting usually requires several questions and answers, that could be a lot of emails
pread in the time, and maybe the people subscribed want to be informed about bugs, fixes, new things introduced
or proposed, but maybe are not interested in other things.

If you have enough time or you prefer not to join Slack, you can also ask questions at StackOverflow.
With slack being an hybrid between chat and forum, we can contact each other faster and troubleshoot things in less time.

When troubleshooting, if we determine that there is a bug, or something to improve, you can report it a GitHub.
Of course, it is not a good idea either to keep a bug report (once confirmed) just in Slack since it could be forgotten,
so lets put them in GitHub.

**Pull Requests:**

If you have a functionality or bugfix you think would be worth including in Ktor, you can create a PR.

Have in mind that we usually review and merge PRs in batches, so the PR could be outstanding for a few weeks.
But still we encourage you to contribute if you can!

If you have a bugfix that need to use right away we recommend you to fork Ktor, compile it yourself,
and temporarily publish a patched version in your own artifactory, bintray or similar and use that version
until it is merged and a new version released or prereleased (since the timing might not be aligned with your needs).

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

## I get a `io.ktor.server.engine.BaseApplicationResponse$ResponseAlreadySentException: Response has already been sent`
{: #response-already-sent }

This means that you, or a feature or interceptor, have already called `call.respond*` functions and you are calling it
again. 

## How can I subscribe to ktor events?
{: #ktor-events }

There is a page [explaining the Ktor's application-level event system](/advanced/events.html). 

## I get a `Exception in thread "main" com.typesafe.config.ConfigException$Missing: No configuration setting found for key 'ktor'` exception
{: #cannot-find-application-conf }

This means that Ktor was not able to find the `application.conf` file. Re-check that it is in the `resources` folder,
and that the resources folder is marked as such.
You can consider to set-up a project using the [project generator](/quickstart/generator.html) or the [IntelliJ plugin](/quickstart/intellij-idea/plugin.html)
to have a working project as base.

## Can I use ktor on Android?
{: #android-support }

Ktor 0.9.3 and lower is know to work on Android 7 or greater (API 24). It will fail in lower versions like Android 5.

In unsupported versions it would fail with an exception similar to:

```
E/AndroidRuntime: FATAL EXCEPTION: main Process: com.mypackage.example, PID: 4028 java.lang.NoClassDefFoundError: 
io.ktor.application.ApplicationEvents$subscribe$1 at io.ktor.application.ApplicationEvents.subscribe(ApplicationEvents.kt:18) at 
io.ktor.server.engine.BaseApplicationEngine.<init>(BaseApplicationEngine.kt:29) at 
io.ktor.server.engine.BaseApplicationEngine.<init>(BaseApplicationEngine.kt:15) at 
io.ktor.server.netty.NettyApplicationEngine.<init>(NettyApplicationEngine.kt:17) at io.ktor.server.netty.Netty.create(Embedded.kt:10) at 
io.ktor.server.netty.Netty.create(Embedded.kt:8) at io.ktor.server.engine.EmbeddedServerKt.embeddedServer(EmbeddedServer.kt:50) at 
io.ktor.server.engine.EmbeddedServerKt.embeddedServer(EmbeddedServer.kt:40) at 
io.ktor.server.engine.EmbeddedServerKt.embeddedServer$default(EmbeddedServer.kt:27)
```

For more informatio, check [Issue #495](https://github.com/ktorio/ktor/issues/495) and [StackOverflow 
question](https://stackoverflow.com/questions/49945584/attempting-to-run-an-embedded-ktor-http-server-on-android)

## CURL -I returns a 404 Not Found
{: #curl-head-not-found }

`CURL -I` that is an alias of `CURL --head` that performs a `HEAD` request.
By default Ktor doesn't handle `HEAD` requests for `GET` handlers, so you might get something like: 

```kotlin
curl -I http://localhost:8080
HTTP/1.1 404 Not Found
Content-Length: 0
```

for:

```kotlin
routing {
    get("/") { call.respondText("HELLO") }
}
```

Ktor can automatically handle `HEAD` requests, but requires you to first install the [`AutoHeadResponse` feature](/features/autoheadresponse.html).

## I get an infinite redirect when using the `HttpsRedirect` feature
{: #infinite-redirect }

The most probable cause is that your backend is behind a reverse-proxy or a load balancer, and that the reverse-proxy
is making normal HTTP requests to your backend, thus the HttpsRedirect feature inside your Ktor backend believes
that it is a normal HTTP request and responds with the redirect.

Normally, reverse-proxies send some headers describing the original request (like it was https, or the original IP address),
and there is a feature [`XForwardedHeadersSupport`](/features/forward-headers.html)
to parse those headers so the [`HttpsRedirect`](/features/https-redirect.html) feature knows that the original request was HTTPS.
 

