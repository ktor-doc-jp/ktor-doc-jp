---
title: Auto Head Response
keywords: Home Page
tags: [feature]
sidebar: mydoc_sidebar
permalink: /features/autoheadresponse.html
summary: Automatically handles HEAD responses using application's GET routes
---

## Description

Ktor can automatically provide responses to `HEAD` requests for existing routes that have the `GET` verb defined. 

## Usage

To enable automatic `HEAD` responses, install the `AutoHeadResponse` feature


```kotlin
fun Application.main() {
  ...
  install(HeadRequestSupport) 
  ...
}
```

## Configuration options

None.

## Under the covers

This feature automatically responds to `HEAD` requests by routing as if it were `GET` response and discarding 
the body. Since any `FinalContent` produced by the system has lazy content semantics, it does not incur in any performance
costs for processing a `GET` request with a body. 
