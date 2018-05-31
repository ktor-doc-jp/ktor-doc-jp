---
title: Changelog
caption: Changelog
category: quickstart
permalink: /quickstart/changelog.html
priority: 98
---

## 0.9.2

* New auth DSL, more suspendable functions (such as verify/validate)
* RoutingResolveTrace for introspecting routing resolution process
* HTTP client improvements and bugfixes (DSL, reconnect, redirect, cookies, websockets and more)
* CIO http client pipelining support, chunked and more
* CIO initial TLS support
* Session authentication provider
* OAuth2: introduced the ability to generate and verify state field
* OAuth: fix scopes parameter to conform to RFC (#329)
* OAuth2: fix bug with double scopes encoding (#370)
* OAuth2: add the ability to intercept redirect URL
* CORS: introduced the allowSameOrigin option
* Auth: provide application call as receiver for validating functions (#375 and related)
* Test host reworked, handleRequest reads the body and redirects the exceptions correctly
* Servlets: fixed inputStream acquisition, fixed error handling
* Java 9 compatibility improved (no modules yet)
* Digest auth fixes (#380)
* Log running connectors details for better development experience (#318)
* Last-Modified header and related functionality to work in proper GMT time zone (#344)
* IncomingContent is deprecated
* URLBuilder fixes and improvements
* Documentation improvements
* Performance optimizations (Netty, CIO server backends)
* CIO server improved stability
* Encrypted session support (SessionTransportTransformerEncrypt)
* Empty (null) model for freemarker (#291)
* ContentNegotiation missing Accept header support (#317)
