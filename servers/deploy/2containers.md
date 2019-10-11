---
title: コンテナ
caption: コンテナ
category: servers
permalink: /servers/deploy/containers.html
ktor_version_review: 1.0.0
---

## Docker
{: #docker}

Docker はコンテナエンジンです。
レイヤー化された軽量な環境で、独立分離されたファイルシステム、OS、およびリソースを使用して、アプリケーションを
コンテナ化および実行ができます。

モノリシックなサービスの場合は `Dockerfile` を作成し、コンテナ間でやり取りが必要な場合 (データベースや redis など) は
`docker-compose.yml` を作成します。

まずは、あなたのアプリケーションの [fat-jar ファイル](/servers/deploy/packing/fatjar) を作成します。
次に、下記のような `Dockerfile` を作成します。

{% capture docker-file %}{% include docker-sample.md %}{% endcapture %}

{% include tabbed-code.html
    tab1-title="Dockerfile" tab1-content=docker-file
    no-height="true"
%}

Docker コンテナをデプロイするには、 [Docker コンテナの作成](/quickstart/quickstart/docker.html) を参照してください。

### Nginx
{: #nginx}

複数のドメインで Docker を使用する場合、 [nginx-proxy](https://github.com/jwilder/nginx-proxy) イメージと
[letsencrypt-nginx-proxy-companion](https://github.com/JrCs/docker-letsencrypt-nginx-proxy-companion) イメージを使用して、
単一のマシン / IP で複数のドメイン / サブドメインを提供したり、暗号化して HTTPS 通信を可能にしたりできます。

`nginx-proxy` と `letsencrypt-nginx-proxy-companion` を設定したうえで、
`docker-compose.yml` を下記のように記述します。
(他のサービスは省いています。)

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

`docker-compose up -d` で起動すると、サービスの起動が失敗したりシステムがリブートした場合でも、
自動的にサービスが再起動されます。

指定されたドメインの DNS があなたのサーバと紐付いていて、 `nginx-proxy` 等が正しく設定されているならば、
letsencrypt-nginx-proxy-companionは letsencrypt と通信し、証明書を自動的に取得して設定します。
https://mydomain.com/ のようにアクセスすると、 nginx が SSL 証明書を処理して プレーンな HTTP を経由してあなたのサーバへ
アクセスするので、 HTTP 通信だけサービスに HTTPS からアクセスできるようになります。

## Tomcat
{: #tomcat}

[war ファイル](/servers/deploy/packing/war) を作成し Tomcat の `webapps` ディレクトリに配置する必要があります。

例:
<https://github.com/ktorio/ktor-samples/tree/master/deployment/tomcat-war>
{: .note.example}

## Jetty
{: #jetty}

[war ファイル](/servers/deploy/packing/war) を作成し、 Jetty の `webapps` ディレクトリに配置する必要があります。

例:
<https://github.com/ktorio/ktor-samples/tree/master/deployment/jetty-war>
{: .note.example}
