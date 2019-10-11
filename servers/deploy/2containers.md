---
title: コンテナ
caption: コンテナ
category: servers
permalink: /servers/deploy/containers.html
ktor_version_review: 1.0.0
---

## Docker
{: #docker}

Dockerはコンテナエンジンです。
Dockerを使うことで、サンドボックス化された軽量な環境で、独立したファイルシステム・OS・リソースのもと、
アプリケーションをパッケージジング・実行することができます。

通常、モノリシックなサービスごとに`Dockerfile`を作る必要があります。
また、コンテナが他のサービス（例えばDBやRedis）と連携する必要があるとき`docker-compose.yml`が使えます。

まず初めにアプリケーションについて[fat-jar file](/servers/deploy/packing/fatjar)を作成する必要があります。
そして以下のように`Dockerfile`も作成する必要があります。

{% capture docker-file %}{% include docker-sample.md %}{% endcapture %}

{% include tabbed-code.html
    tab1-title="Dockerfile" tab1-content=docker-file
    no-height="true"
%}

Dockerをシンプルな形でデプロイするため、[dockerクイックスタート](/quickstart/quickstart/docker.html)を読むとよいでしょう。

### Nginx
{: #nginx}

Dockerを複数ドメインで利用する場合、
[nginx-proxy](https://github.com/jwilder/nginx-proxy)イメージと
[letsencrypt-nginx-proxy-companion](https://github.com/JrCs/docker-letsencrypt-nginx-proxy-companion)イメージを
使うことで、単一のマシン/ip内で複数ドメイン/サブドメインを利用し、Let's Encryptを利用して自動的にHTTPSを使ったりしたくなるかもしれません。

nginx-proxyとletsencrypt-nginx-proxy-companionの設定後、docker-compose.ymlファイルは（その他別のサービスがない場合）例えば以下のようになります:

{% capture docker-compose-yml %}
```yaml
version: '2'
services:
  web:
    build:
      context: ./
      dockerfile: Dockerfile
    expose:
      - 8080
    environment:
      - VIRTUAL_HOST=mydomain.com
      - VIRTUAL_PORT=8080
      - LETSENCRYPT_HOST=mydomain.com
      - LETSENCRYPT_EMAIL=myemail@mydomain.com
    networks:
      - reverse-proxy
    restart: always

networks:
  backend:
  reverse-proxy:
    external:
      name: reverse-proxy
```
{% endcapture %}

{% include tabbed-code.html
    tab1-title="docker-compose.yml" tab1-content=docker-compose-yml
    no-height="true"
%}

`docker-compose up -d`で起動することができ、
サービスが失敗するかシステム再起動後にはコンテナは再起動されます。

指定したドメインのDNSがあなたのサーバーを指しており、nginx-proxyとletsencrypt-nginx-proxy-companionが正常に設定されているならば、
letsencrypt companionはletsencryptにアクセスし、証明書を自動的に取得・設定します。
そうすることで、HTTP onlyのサービスにhttps://mydomain.com/経由でアクセスできるようになります。
nginxがSSL証明書をハンドリングするため、サーバーにプレーンなHTTP経由でアクセスできます。

## Tomcat
{: #tomcat}

[warファイル](/servers/deploy/packing/war)を生成する必要があり、Tomcatの`webapps`フォルダ内に配置する必要があります。

完全な例:
<https://github.com/ktorio/ktor-samples/tree/master/deployment/tomcat-war>
{: .note.example}

## Jetty
{: #jetty}

[warファイル](/servers/deploy/packing/war)を生成する必要があり、Jettyの`webapps`フォルダ内に配置する必要があります。

完全な例:
<https://github.com/ktorio/ktor-samples/tree/master/deployment/jetty-war>
{: .note.example}
