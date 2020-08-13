---
title: WebSockets
category: clients
caption: WebSockets
feature:
  artifact: io.ktor:ktor-client-websockets:$ktor_version
  class: io.ktor.client.features.websocket.ClientWebSocketSession
  method: io.ktor.client.features.websocket.ws
ktor_version_review: 1.2.0
---

WebSocket を用いてサーバ間と双方向通信を行う機能を提供しています。
詳細は専用の [WebSocket](/clients/websockets.html) のページを参照してください。

{% include 
    mpp_feature.html
    targets="common,jvm,native,js"
    base="ktor-client-websockets"
    classifiers=",-jvm,-native,-js"
%}
