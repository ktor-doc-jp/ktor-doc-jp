---
title: Authentication
caption: Developing Custom Authentication
category: advanced
permalink: /advanced/authentication.html
---

Prerequisite reading: [Advanced Pipeline](/advanced/pipeline.html)

`Authentication` feature creates an `AuthenticationPipeline` which is executed right after `Features` phase
in call pipeline. All authentication protocols like basic, digest, oauth are implemented as interceptors on AuthenticationPipeline.

AuthenticationPipeline has two phases:

* `CheckAuthentication` – Phase for checking if user is already authenticated before all mechanisms kicks in
* `RequestAuthentication` – Phase for authentications mechanisms to plug into

The subject of the pipeline is an `AuthenticationContext` instance.

Protocol:

* Auth provider interceptor tries to find a `Principal` in the context of the current call.
* If the principal is found, it is returned and the pipeline is finished
* If the principal is not found, the provider will add a challenge to AuthenticationContext
* At the end of the pipeline, if there is no principal, we start calling challenges in order
* Whichever challenge succeeds first wins. 

Example:

* Basic auth looks into the `Authorization` header 
* If it's missing or invalid or the user is not recognized, 401 Unauthorized is sent back to the user and the current call ends
* Browser shows a dialog, and after credentials are provided it makes a new HTTP request with a proper `Authorization` header
* Basic auth provider now gets a header, extract credentials and verifies them. 
* If the credentials are valid, the principal is attached to a call. If not, a 401 is sent back again.


