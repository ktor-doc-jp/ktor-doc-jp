---
title: Multiplatform
caption: Multiplatform
category: kotlinx
permalink: /kotlinx/multiplatform.html
toc: true
---

Kotlin Multi-Platform Projects (also called Kotlin MPP):

Reference: <https://kotlinlang.org/docs/reference/multiplatform.html>

Starting with Kotlin 1.2, there is an experimental multiplatform support.
The idea behind it is to be able to write common code with a subset of common APIs available on all the platforms,
and then per platform specific things.

Multiplatform projects add a couple of new keywords: expect and actual.

expect is available for common projects to be able to define APIs that will be available in common projects,
but will have specific implementations per platform.
actual is available for non-common projects (JVM, JS and Native) and must match the expect structure in each platform
that will be supported.

Starting with Kotlin 1.3-M2, there is a new multiplatform project model, that aims to allow much simpler multiplatform projects:
* <https://discuss.kotlinlang.org/t/kotlin-1-3-m2-new-multiplatform-projects-model/9264>{: target="_blank"}
* <https://github.com/h0tk3y/k-new-mpp-samples>{: target="_blank"}
