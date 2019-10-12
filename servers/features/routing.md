---
title: ルーティング
caption: HTTPリクエストに対する構造化されたハンドリング
category: servers
permalink: /servers/features/routing.html
feature:
  artifact: io.ktor:ktor-server-core:$ktor_version
  class: io.ktor.routing.Routing
redirect_from:
- /features/routing.html
ktor_version_review: 1.0.0
---

ルーティングはアプリケーションにインストールすることで、リクエストのハンドリング機能をかんたんに構築できるようにする機能です。

このページではルーティング機能について説明します。
リクエストに関する情報の抽出方法と、ルート内での有効なレスポンスの生成方法は[リクエスト]・[レスポンス]ページに記載されています。

[リクエスト]: /servers/calls/requests.html
[レスポンス]: /servers/calls/responses.html

```kotlin
    application.install(Routing) {
        get("/") {
            call.respondText("Hello, World!")
        }
        get("/bye") {
            call.respondText("Good bye, World!")
        }
    }
```

`get`, `post`, `put`, `delete`, `head`, `options` 関数は柔軟で強力なルーティング定義するための便利なショートカット関数です。
特に、`get`は`route(HttpMethod.Get, path) { handle(body) }`のエイリアスです。
`body`は`get`関数に渡されれるラムダです。

{% include feature.html %}

## ルーティングツリー

ルーティングはツリー状に構成されています。
リクエストの処理に関する極めて複雑なルールもカバーできるような再帰的なマッチングシステムが使われています。
ツリーはノードとセレクタによって構成されています。
ノードはハンドラーとインターセプタを含んでおり、セレクタはノードに関連付いています。
もしセレクタが現在のルーティングの評価コンテキストに一致したならば、アルゴリズムはセレクタに関連付いているノードを選択します。

ルーティングはDSLを利用してネストする方式で定義できます:
  
```kotlin
route("a") { // matches first segment with the value "a"
  route("b") { // matches second segment with the value "b"
     get {…} // matches GET verb, and installs a handler 
     post {…} // matches POST verb, and installs a handler
  }
}
```
  
```kotlin
method(HttpMethod.Get) { // matches GET verb
   route("a") { // matches first segment with the value "a"
      route("b") { // matches second segment with the value "b"
         handle { … } // installs handler
      }
   }
}
```  

ルート解決のアルゴリズムは、セレクタが一致しない場合はサブツリーを排除しながら、ノードを再帰的に走査します。

ビルダー関数群:

* `route(path)` – パスセグメントマッチャーを追加します。[paths](#path)をご覧ください。
* `method(verb)` – HTTPメソッドマッチャーを追加します。
* `param(name, value)` – クエリパラメータの特定の値に対するマッチャーを追加します。
* `param(name)` – クエリパラメータの存在チェックとその値のキャプチャを行うマッチャーを追加します
* `optionalParam(name)` – 存在した場合はクエリパラメータの値をキャプチャするようなマッチャーを追加します
* `header(name, value)` – HTTPヘッダーの特定の値に対するマッチャーを追加します。[quality](#quality)をご覧ください。

## パス

手動でルーティングツリーを構築するのは非常に不便です。
そのため、`route`関数というものがあり、これは _path_ を使うことでシンプルな方法でほとんどのユースケースに対応します。

`route` 関数（と対応するHTTPメソッドのエイリアス）は`path`をパラメータとして受け取り、
`path`はルーティングツリーを構築するために使われます。
初めに、`'/'`デリミターによって、パスセグメント群に分割されます。
各セグメントはネストされたルーティングノードを生成します。

以下2つは等価です:

```kotlin
route("/foo/bar") { … } // (1)

route("/foo") {
   route("bar") { … } // (2)
}
```

#### パラメータ

パスは _パラメータ_　を含めることができます。
パラメータは特定のパスセグメントに一致し、その値をapplication callの`parameters`プロパティへとキャプチャします。:

```kotlin
get("/user/{login}") {
   val login = call.parameters["login"]
}
```

上のケースでユーザエージェントがGETメソッドを使って`/user/john`をリクエストした場合、
ルートはマッチし`parameters`プロパティは`"login"`キーに対して`"john"`の値を持ちます。

#### オプショナル、ワイルドカード、テイルカード

パラメータとパスセグメントは、オプショナルになるか、URIの残り全体をキャプチャするかのどちらかになります。

* `{param?}` – オプショナルなパスセグメントです。もし存在した場合はパラメータにキャプチャされます
* `*` – ワイルドカードです。任意のセグメントにマッチしますが、必ず値は存在する必要があります。
* `{...}`– テイルカードです。URIの残り全体にマッチします。最後になる必要があり、空を許容します。
* `{param...}` – キャプチャされるテイルカードです。URIの残り全体にマッチし、各パスセグメントの値群を`parameters`へと入れます。`param`というキーを使っており、`call.parameters.getAll("param")`ですべての値を取得できます。
 
例:

```kotlin
get("/user/{login}/{fullname?}") { … } 
get("/resources/{path...}") { … } 
```

## クオリティ

いくつかのルートが同じHTTPリクエストにマッチすることが起こりえます。

１つの例としては、`Accept`HTTPヘッダーに対するマッチングです。
特定のプライオリティ（クオリティ）で複数の値を持ち得ます。

```kotlin
accept(ContentType.Text.Plain) { … }
accept(ContentType.Text.Html) { … }
```

ルーティングのマッチングアルゴリズムは、特定のHTTPリクエストがルーティングツリーの特定のパスにマッチするかチェックしているだけではなく、
マッチのクオリティの計算やベストクオリティのルーティングノードの選択も行っています。
上のルートの例でいうと、いずれのAcceptヘッダーにマッチするのかを選択します。
リクエストヘッダー`Accept: text/plain; q=0.5, text/html`は`text/html`にマッチします。
なぜならHTTPヘッダーのクオリティファクターが`text/plain`に対する低いクオリティを指定しているからです（デフォルトは1.0です）。

ヘッダー`Accept: text/plain, text/*`は`text/plain`にマッチします。
ワイルドカードによるマッチは直接的なマッチよりも具体性が低いと考えられます。
そのため、ルーティングマッチングアルゴリズムはワイルドカードを低いクオリティだと判断します。

他の例としては名前のあるエンティティ（例えばuser）に短いURLを生成しつつ、"settings"のような特定のページも使えるようにする場合があります。
例としては

* `https://twitter.com/kotlin` – "kotlin"という名前のユーザを表示します
* `https://twitter.com/settings` - 設定ページを表示します

これは以下のように実装できます:

```kotlin
get("/{user}") { … }
get("/settings") { … }
```

パラメータは定数文字列よりも低いクオリティだと考えられるため、
`/settings`は両方にマッチしながらも2つめのルートのほうが選択されます。

## インターセプション

ルーティングノードが選択されると、ルーティングシステムはノードを実行するための特別なパイプラインを構築します。
このパイプラインは、選択されたノードやノードにインストールされたインターセプタのためのハンドラー群で構成されており、
上から下の順で、ルートから選択されたノードまでのパスを構築します。

```kotlin
route("/portal") {
   route("articles") { … }
   route("admin") {
      intercept(ApplicationCallPipeline.Features) { … } // verify admin privileges
      route("article/{id}") { … } // manage article with {id}
      route("profile/{id}") { … } // manage profile with {id}
   }
}
```

上のルーティングツリーについて考えると、
リクエストURIが`/portal/articles`で始まるとき、ルーティングはcallを普通に処理しますが、
もしリクエストが`/portal/admin`セクションに一致する場合、まず初めにインターセプタを実行しユーザがアドミンページにアクセスする上で十分な権限を持っているかをチェックします。

他の例としては、JSONシリアライゼーション機能を`/api`セクションにインストールし、
`/user/{id}`セクションでデータベースからユーザを読み込みcallのattributeに設定するなどの使い方が考えられます。

## 拡張性

`ktor-server-core`モジュールは、メソッド・パス・ヘッダー・クエリパラメータにマッチするたくさんの基本的なセレクタを含んでいますが、
より複雑なロジックに対応するために自身で定義したセレクタをかんたんに追加することができます。
`RouteSelector`を実装し、ビルトインのものに似たビルダー関数を作成できます。

Pathのパース処理は拡張できません。

## ルーティングの決定の追跡
{: #tracing }

なぜルートが実行されないのかわからない問題があった場合のため、Ktorは`trace`関数をルーティング機能内で提供します。

```kotlin
routing {
    trace { application.log.trace(it.buildText()) }
}
```

このメソッドはcallが実行されるたびに呼び出され、行われた決定に関するトレースログを出力します。
例としては、以下のルーティング設定において:

```kotlin
routing {
    trace { application.log.trace(it.buildText()) }
    get("/bar") { call.respond("/bar") }
    get("/baz") { call.respond("/baz") }
    get("/baz/x") { call.respond("/baz/x") }
    get("/baz/x/{optional?}") { call.respond("/baz/x/{optional?}") }
    get("/baz/{y}") { call.respond("/baz/{y}") }
    get("/baz/{y}/value") { call.respond("/baz/{y}/value") }
    get("/{param}") { call.respond("/{param}") }
    get("/{param}/x") { call.respond("/{param}/x") }
    get("/{param}/x/z") { call.respond("/{param}/x/z") }
    get("/*/extra") { call.respond("/*/extra") }

}
```

`/bar`へのリクエストがあった場合のアウトプットは以下のようになります:

```
Trace for [bar]
/, segment:0 -> SUCCESS @ /bar/(method:GET))
  /bar, segment:1 -> SUCCESS @ /bar/(method:GET))
    /bar/(method:GET), segment:1 -> SUCCESS @ /bar/(method:GET))
  /baz, segment:0 -> FAILURE "Selector didn't match" @ /baz)
  /{param}, segment:0 -> FAILURE "Better match was already found" @ /{param})
  /*, segment:0 -> FAILURE "Better match was already found" @ /*)
```

プロダクションで利用するときは、この関数を除外するか無効化することを忘れないでください
{: .note }
