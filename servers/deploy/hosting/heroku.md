---
title: Heroku
caption: Heroku
category: servers
permalink: /servers/deploy/hosting/heroku.html
ktor_version_review: 1.0.0
---

Heroku用のクイックスタートリポジトリがあります: <https://github.com/orangy/ktor-heroku-start>
{: .note.example}

## 準備

Herokuを使うため、Java、Maven/Gradle、[Heroku CLI](https://devcenter.heroku.com/articles/heroku-cli)が必要です。

Heroku設定でpublic keyを設定する必要もあります。

`heroku --version`コマンドを試し、コマンドラインがインストールされているか確認できます。:

```
> heroku --version
heroku-cli/6.15.36 (darwin-x64) node-v9.9.0
```

プロジェクトと依存関係を定義するため、`app.json`ファイルも必要とします:
```json
{
  "name": "Start on Heroku: Kotlin",
  "description": "A barebones Kotlin app, which can easily be deployed to Heroku.",
  "image": "heroku/java",
  "addons": [ "heroku-postgresql" ]
}
```

何を実行するかを定義するため`Procfile`も必要とします:
```
web:    java -jar target/helloworld.jar
```

javaバージョンを定義するため`system.properties`ファイルも必要とします:

```
java.runtime.version=1.8
```

## ローカル実行

`.env`という名前のファイルやその他（開発用に必要な）ファイルもあります。
これらのファイルはHerokuがアプリケーションに渡す環境変数を含んでいます。
例えば、クイックスタートとしては以下のようになります:

```properties
PORT=8080
JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/java_database_name
```

ローカルへインストールしたpostgresqlはuser/passwordを持つため、jdbc urlを以下のように変更する必要があります:
```properties
JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/java_database_name?user=user&password=password
```

まず初めにdatabaseを作成する必要があります:

```
> psql -c "CREATE DATABASE java_database_name;"

CREATE DATABASE
```

これらのファイル群とともに、GradleやMavenで[fat-jar](/servers/deploy/packing/fatjar)を作成し、
`Procfile`が正しいファイルを指定するように調整します。

jarのビルド後、Unixシステムだと`heroku local:start`を使うことでサーバーを起動できます。

## デプロイ

最初にHerokuアプリケーションの作成またはgit remoteの設定が必要です。
`heroku create`コマンドはアプリケーションをランダムな名前で作成し、レポジトリに対するgit remoteを設定します。
`heroku create`実行すると、以下のような内容ができます:

```
> heroku create
Creating app... done, ⬢ demo-demo-12345
https://demo-demo-12345.herokuapp.com/ | https://git.heroku.com/demo-demo-12345.git
```

このコマンドは`heroku` remoteをあなたのgit cloneに追加します:

```
> cat .git/config
...
[remote "heroku"]
	url = https://git.heroku.com/demo-demo-12345.git
	fetch = +refs/heads/*:refs/remotes/heroku/*
```

その後、`heroku` remoteにgit changeをpushする必要があります。
push時にビルドが実行されます:

```
> git push heroku master
Counting objects: 90, done.
Delta compression using up to 4 threads.
Compressing objects: 100% (59/59), done.
Writing objects: 100% (90/90), 183.08 KiB | 5.55 MiB/s, done.
Total 90 (delta 21), reused 0 (delta 0)
remote: Compressing source files... done.
remote: Building source:
remote:
remote: -----> Java app detected
remote: -----> Installing JDK 1.8... done
remote: -----> Executing: ./mvnw -DskipTests clean dependency:list install
...
remote:        [INFO] BUILD SUCCESS
remote:        [INFO] ------------------------------------------------------------------------
remote:        [INFO] Total time: 49.698 s
remote:        [INFO] Finished at: 2018-03-23T04:33:01+00:00
remote:        [INFO] Final Memory: 41M/399M
remote:        [INFO] ------------------------------------------------------------------------
remote: -----> Discovering process types
remote:        Procfile declares types -> web
remote:
remote: -----> Compressing...
remote:        Done: 60.7M
remote: -----> Launching...
remote:        Released v4
remote:        https://demo-demo-12345.herokuapp.com/ deployed to Heroku
remote:
remote: Verifying deploy... done.
To https://git.heroku.com/demo-demo-12345.git
 * [new branch]      master -> master
```

`heroku open`を実行し、アプリケーションをあなたのブラウザ上で開くこともできます:

```
heroku open
```

この場合、https://demo-demo-12345.herokuapp.com/ が開かれます。

Herokuは特定のポートの代わりに`PORT`という環境変数をセットすることでポート指定する必要があることを覚えておいてください。<br/>
組み込みサーバ利用時には`System.getenv`を使う必要がありますが、
`application.conf`を使う場合は`ktor.deployment.port = ${PORT}`をセットする必要があります。<br/>
[設定内で環境変数を利用](/servers/configuration.html#environment-variables)ページをご参照ください。
{: .note}
