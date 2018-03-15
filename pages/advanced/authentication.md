---
title: Authentication
caption: Developing Custom Authentication
section: Advanced
permalink: /drafts/advanced/authentication.html
---

Prerequisite reading: [Advanced Pipeline](advanced/pipeline)

`Authentication` feature creates an `AuthenticationPipeline` which is executed right after `Infrastructure` phase
in call pipeline. All authentication protocols like basic, digest, oauth are implemented as interceptors on AuthenticationPipeline.

AuthenticationPipeline has two phases:

* `CheckAuthentication` – something I can't remember why it is there, will investigate
* `RequestAuthentication` – where all the work done

The subject of the pipeline is an `AuthenticationContext` instance.

Protocol:

* Auth provider interceptor tries to find a `Principal` in the context of current call.
* If principal is found, it is returned and pipeline is finished
* If principal is not found, provider will add a challenge to AuthenticationContext
* In the end of the pipeline, if there is no principal, we start calling challenges in order
* Whichever challenge succeeds first wins. 

Example:

* Basic auth looks into `Authorization` header 
* If it's missing or invalid or user not recognized, 401 Unauthorized is sent back to user and current call ends
* Browser shows a dialog, and after credentials are provided it makes a new HTTP request with a proper `Authorization` header
* Basic auth provider now gets a header, extract credentials and verifies them. 
* If credentials are valid, principal is attached to a call. If not, 401 is sent back again.


