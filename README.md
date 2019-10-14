Kotlin製Web Frameworkである[Ktor](https://ktor.io)のドキュメントの[日本語翻訳版](https://jp.ktor.work)です。

## Motivation 👀

Ktorは今後Kotlin製Webフレームワークとして有力な選択肢になると感じ、
この日本語訳があることで日本においてKotlinが普及するきっかけになれば面白いなと思い翻訳しました。
（Rails, Vue.js, Nuxt.jsなども、日本語訳があることが普及の追い風となったのではないかと考えています）

Kotlinを初めて触る人だけでなく、普段Android開発をKotlinでしている人がAPIサーバを同じくKotlinでサクッと作りたい場合などにも、
このドキュメントがあることで導入のハードルを下げることができるのではないかと思っています。

## 募集 📖 

翻訳を手伝っていただけるContributorの方を募集しています。
Pull Request絶賛お待ちしています！

また、Maintainer（元ドキュメントへの追従を行ったり、レビューを行う人）も募集しています。
（その場合は個人リポジトリではなく組織リポジトリにし、共同運営していきたいと思っています。）

またドメイン費用や打ち上げ費用を援助してくださるSponsorも募集しています。

## 議論・質問 💬

Issueや[日本KotlinユーザグループのSlack](http://kotlinlang-jp.herokuapp.com/)の`ktor`チャンネルでお待ちしてます。
興味本位の質問でも大丈夫ですので気軽に質問ください。
その他個別の連絡はGithubに公開しているEmail宛にください。

## PullRequestルール ✅

* 修正対象: 基本的にどんなものでも大丈夫です
  * 英語になっているページの翻訳
  * わかりにくい文章・翻訳を改善（ **元の翻訳は一切気にせずよりわかりやすいと思う訳をご提案ください**）
  * typoの修正
  * [元ドキュメント](https://ktor.io)の修正への追従
* ルール: 特にありません
  * タイトル・説明・コミットメッセージ等は日本語で作成していただいて大丈夫です。記法に指定はありません。
* 方針
  * 原文を直訳するよりも、多少情報が落ちる・変更されるとしても意訳して読みやすくしたほうがいいと思っています。

## 開発方法 💻

`./develop.sh` を実行後 `localhost:4000` で修正中のドキュメントが閲覧できます。

## 反映済みコミット

元ドキュメントの以下コミットまでは反映済み。
https://github.com/ktorio/ktorio.github.io/commit/6576021ee89cb90d1a77b7fe705394b0970154e7

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
    <td align="center"><a href="https://github.com/lasta"><img src="https://avatars2.githubusercontent.com/u/2967161?v=4" width="100px;" alt="Atsushi Koshikizawa"/><br /><sub><b>Atsushi Koshikizawa</b></sub></a><br /><a href="#platform-lasta" title="Packaging/porting to new platform">📦</a> <a href="#platform-lasta" title="Packaging/porting to new platform">📦</a></td>
    <td align="center"><a href="https://github.com/kyou-today"><img src="https://avatars0.githubusercontent.com/u/43880251?v=4" width="100px;" alt="misato/キョウ"/><br /><sub><b>misato/キョウ</b></sub></a><br /><a href="#userTesting-kyou-today" title="User Testing">📓</a> <a href="https://github.com/doyaaaaaken/ktor-doc-jp/commits?author=kyou-today" title="Documentation">📖</a></td>
    <td align="center"><a href="https://github.com/pokotsun"><img src="https://avatars1.githubusercontent.com/u/22976888?v=4" width="100px;" alt="pokotsun"/><br /><sub><b>pokotsun</b></sub></a><br /><a href="https://github.com/doyaaaaaken/ktor-doc-jp/commits?author=pokotsun" title="Documentation">📖</a></td>
  </tr>
</table>

<!-- markdownlint-enable -->
<!-- prettier-ignore-end -->
<!-- ALL-CONTRIBUTORS-LIST:END -->
