---
title: 証明書
caption: 証明書
category: servers
keywords: certificates https
permalink: /servers/certificates.html
---

## JKS file format

JKS (Java KeyStore)はJavaとKtorで利用される証明書のフォーマットです。

この種類の証明書を変換・管理するためには[`keytool`](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html)が利用できます。

## CER file format

```bash
keytool -import -v -trustcacerts -alias keyAlias -file server.cer -keystore cacerts.jks -keypass changeit
```

## SSL

[SSLガイド](/quickstart/guides/ssl.html)により詳細な情報が記載されています。

