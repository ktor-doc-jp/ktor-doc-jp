## 概要 👀

Kotlin製WebFrameworkである[Ktor](https://ktor.io)の[日本語ドキュメント](https://jp.ktor.work)です。

（特にサーバサイドKotlinにおいては）有力な選択肢となるWebフレームワークだと考えており、
この日本語訳があることで日本においてKotlinの普及率が増えるきっかけになれば面白いなと思い翻訳しました。
（RailsやNuxt.jsなども、日本語訳があることが普及の追い風となったのではないかと考えています）

翻訳を手伝っていただけるContributerの方を（また、Maintenerも）募集しています。
また、Issue、Pull Request絶賛お待ちしています！

## PullRequestについて ✅

* 着手方法
  * タイトル・説明・コミットメッセージ等は日本語で作成していただいて大丈夫です。
  * コミットメッセージに指定はありませんが、翻訳元ページがわかるようURL等を入れておくといいかもしれません。
  * 他の方と着手箇所が被ると悲しいことになるので、空コミットを入れてタイトルの先頭に`[WIP]`とつけPRを作成することを推奨します。（PR作成が早かったほうを優先になります）
  * PRの説明文のところに着手対象のURLがあるとわかりやすいのでありがたいです。
* 着手対象
  * メインとなる対象は現在英語になっているページです。
  * 翻訳済みのページでも改善できそうな訳があれば、そちらももちろん対象となります。
* 心構え的な
  * 原文を直訳するよりも、多少情報が落ちたとしても意訳して読みやすくするほうが良いと考えています
  * 仮に下手な翻訳だったとしても読み手にとっては無いよりもはるかに有益なので、自信が無くても是非挑戦してみてください！
  * 内容がわからないところは部分的に英語のまま残すのもアリだと思います。

## 開発方法 💻

`./develop.sh` を実行後 `localhost:4000` で修正中のドキュメントが閲覧できます。

## 反映済み

元ドキュメントの以下コミットまでは反映済み。
https://github.com/ktorio/ktorio.github.io/commit/6576021ee89cb90d1a77b7fe705394b0970154e7

## 議論・質問 💬

Issueか、[日本KotlinユーザグループのSlack](http://kotlinlang-jp.herokuapp.com/)の`ktor`チャンネルでお待ちしてます。

## Contributors ✨

`SELECT * FROM contributors ORDER BY commited_page_count DESC, last_commited_at ASC`

```
💻: Committer
📦: 4page
📓: 2page
📖: 1page
🐛: typo
```

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tr>
    <td align="center"><a href="https://github.com/doyaaaaaken"><img src="https://avatars3.githubusercontent.com/u/5428401?v=4" width="100px;" alt="kenta.koyama"/><br /><sub><b>kenta.koyama</b></sub></a><br /><a href="https://github.com/doyaaaaaken/ktor-doc-jp/commits?author=doyaaaaaken" title="Code">💻</a></td>
    <td align="center"><a href="https://github.com/lasta"><img src="https://avatars2.githubusercontent.com/u/2967161?v=4" width="100px;" alt="Atsushi Koshikizawa"/><br /><sub><b>Atsushi Koshikizawa</b></sub></a><br /><a href="#platform-lasta" title="Packaging/porting to new platform">📦</a> <a href="#userTesting-lasta" title="User Testing">📓</a> <a href="https://github.com/doyaaaaaken/ktor-doc-jp/commits?author=lasta" title="Documentation">📖</a></td>
    <td align="center"><a href="https://github.com/kyou-today"><img src="https://avatars0.githubusercontent.com/u/43880251?v=4" width="100px;" alt="misato/キョウ"/><br /><sub><b>misato/キョウ</b></sub></a><br /><a href="#userTesting-kyou-today" title="User Testing">📓</a> <a href="https://github.com/doyaaaaaken/ktor-doc-jp/commits?author=kyou-today" title="Documentation">📖</a></td>
    <td align="center"><a href="https://github.com/pokotsun"><img src="https://avatars1.githubusercontent.com/u/22976888?v=4" width="100px;" alt="pokotsun"/><br /><sub><b>pokotsun</b></sub></a><br /><a href="https://github.com/doyaaaaaken/ktor-doc-jp/commits?author=pokotsun" title="Documentation">📖</a></td>
  </tr>
</table>

<!-- markdownlint-enable -->
<!-- prettier-ignore-end -->
<!-- ALL-CONTRIBUTORS-LIST:END -->
