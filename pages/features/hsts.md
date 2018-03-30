---
title: HSTS
caption: Enable HTTP Strict Transport Security
keywords: hsts https ssl secure
section: Features
permalink: /features/hsts.html
feature:
    artifact: io.ktor:ktor-server-core:$ktor_version
    class: io.ktor.features.HSTS
---

This feature will add required _HTTP Strict Transport Security_ headers to the request according to the [RFC 6797](https://tools.ietf.org/html/rfc6797).

HSTS policy headers are ignored over insecure HTTP connection. For HSTS to take effect it should be
served over secure (https) connection.
{: .note} 

When browser receives HSTS policy headers, it will no longer attempt to connect to the server with insecure connections 
for the given period of time. 

{% include feature.html %}

### Usage

```kotlin
fun Application.main() {
  ...
  install(HSTS) 
  ...
}
```

The code above installs HSTS with the default configuration.  

### Configuration

* `maxAge` (default is 1 year): duration to tell client to keep host in a list of known HSTS hosts
* `includeSubDomains` (default is true): adds includeSubDomains directive, which applies this policy to this domain and any subdomains
* `preload` (default is false): consents that the policy allows including the domain into web browser [preloading list](https://https.cio.gov/hsts/#hsts-preloading) 
* `customDirectives` (default is empty): any custom directives supported by specific user-agent
