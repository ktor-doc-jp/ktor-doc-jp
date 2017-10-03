---
title: Artifacts
tags: [overview]
sidebar: mydoc_sidebar
permalink: /artifacts.html
summary:  
---

Ktor is divided into modules to allow fine-grained inclusion of dependencies based on the functionality required. 
The typical Ktor application would require `ktor-core` and a corresponding host depending on whether it's self-hosted
 or using an Application Server. 

All artifacts in Ktor belong to `org.jetbrains.ktor` group and hosted on [Bintray](https://bintray.com/kotlin/ktor)

[![Download](https://api.bintray.com/packages/kotlin/ktor/ktor/images/download.svg?version={{site.ktor_version}})](https://bintray.com/kotlin/ktor/ktor/{{site.ktor_version}})
    
Ktor is split into several groups of modules:

* `ktor-core` is a core package where most of the application API and implementation is located. 
* `ktor-hosts` contains modules that support hosting Ktor Application in different hosts: Netty, Jetty, Tomcat, and 
generic servlet. It also contains TestHost for setting up application tests without starting real host.
  * `ktor-jetty` support a deployed or embedded Jetty instance
  * `ktor-netty` supports Netty in an embedded mode
  * `ktor-tomcat` supports Tomcat servers
  * `ktor-servlet` is used by Jetty and Tomcat and allows hosting in generic servlet container
  * `ktor-test-host` allows running application tests faster without starting full host
* `ktor-features` groups modules for features that are optional and may not be required by every application
  * `ktor-auth` provides support for different authentication systems like Basic, Digest, Forms, OAuth 1a and 2
  * `ktor-auth-ldap` adds ability to authenticate against LDAP instance
  * `ktor-freemarker` integrates Ktor with Freemarker templates
  * `ktor-html-builder` integrates Ktor with kotlinx.html builders
  * `ktor-locations` contains experimental support for typed locations
  * `ktor-server-sessions` adds ability to use stateful sessions stored on a server
  * `ktor-websockets` provides support for Websockets
  

See instructions for setting up a project with

* [Maven](getting-started-maven)
* [Gradle](getting-started-gradle)

