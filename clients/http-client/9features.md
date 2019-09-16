---
title: Features
caption: HttpクライアントのFeature
category: clients
permalink: /clients/http-client/features.html
children: /clients/http-client/features/
ktor_version_review: 1.2.0
---

サーバーと同様に、Ktorはクライアントの機能をサポートします。また、同じ設計になっています。
クライアントHTTPリクエストのパイプラインがあり、インターセプターとインストール可能な機能があります。

{% include children_list.html context=page.children %}

## Custom Featureの作成

機能を作成する場合は、[標準のfeature](https://github.com/ktorio/ktor/tree/master/ktor-client/ktor-client-core/common/src/io/ktor/client/features)をリファレンスとして使用できます。

[HttpRequestPipeline.Phases](https://github.com/ktorio/ktor/blob/master/ktor-client/ktor-client-core/common/src/io/ktor/client/request/HttpRequestPipeline.kt)および
[HttpResponsePipeline.Phases](https://github.com/ktorio/ktor/blob/master/ktor-client/ktor-client-core/common/src/io/ktor/client/response/HttpResponsePipeline.kt)を確認することで、
利用可能なインターセプト箇所について理解することもできます
{: .note.tip}
