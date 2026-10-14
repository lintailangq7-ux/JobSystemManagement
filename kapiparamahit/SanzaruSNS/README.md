# 三猿SNS（Java / JSP 版）

Eclipse（Pleiades）+ Tomcat 10 + MySQL 8 で動く版です。Node.js 版（リポジトリ直下の `server.js`）と同じ MySQL のテーブルを使うので、どちらからでも同じデータを見られます。

## 必要なもの

- Pleiades（Eclipse）と Tomcat 10（`Tomcat10_Java17` など）
- MySQL 8.0 以上

## 動かし方

1. **Eclipse に取り込む**
   ファイル → インポート → 一般 → **既存プロジェクトをワークスペースへ** → ルート・ディレクトリーに `SanzaruSNS` フォルダを選ぶ → 完了

2. **MySQL の接続先を設定する**
   `src/main/webapp/WEB-INF/db.properties.example` をコピーして、同じ場所に `db.properties` を作り、`db.password` などを自分の MySQL に合わせて書き換える。
   （`db.properties` はパスワードが入るので Git には登録されません）

3. **実行する**
   プロジェクトを右クリック → 実行 → **サーバーで実行** → `Tomcat10_Java17` を選ぶ
   → ブラウザで http://localhost:8080/SanzaruSNS/ を開く

データベース `hitokoto_sns` とテーブルは、起動したときになければ自動で作られます。

## うまくいかないとき

| 症状 | 確認すること |
|---|---|
| 画面に「MySQL に接続できませんでした」 | `db.properties` のユーザー名・パスワード。直したら Tomcat を再起動 |
| Java のファイルに赤いエラーが大量に出る | プロジェクトのプロパティ → ターゲット・ランタイム で Tomcat 10 にチェック |
| 日本語が文字化けする | プロジェクトのプロパティ → リソース → テキスト・ファイルのエンコード を UTF-8 に |

## 中身

```
src/main/java/sanzaru/
  AppListener.java   起動時に MySQL に接続してテーブルを作る
  AppFilter.java     全リクエスト共通（ログイン確認・CSRF 対策・メッセージ）
  AccountType.java   見猿・言わ猿・聞か猿
  Passwords.java     パスワードのハッシュ化（Node.js 版と同じ scrypt）
  Config.java        db.properties の読み込み
  model/             User, Post（JSP に渡すデータ）
  dao/               Db, UserDao, PostDao（SQL はここだけ）
  servlet/           画面ごとの処理
src/main/webapp/
  WEB-INF/jsp/       画面（JSP）。parts/ は共通部品
  WEB-INF/lib/       MySQL ドライバと JSTL
  css/ js/           見た目と画面の補助
```

| URL | Servlet | 画面 |
|---|---|---|
| `/` | TimelineServlet | ホーム（自分とフォロー中の人の投稿） |
| `/explore` | TimelineServlet | みんなの投稿 |
| `/users` | UsersServlet | ユーザー一覧 |
| `/user?name=xxx` | ProfileServlet | プロフィール（自分なら編集も） |
| `/post?id=1` | PostServlet | 投稿の詳細とコメント |
| `/login` | LoginServlet | ログイン / 新規登録 |
| `/post/new` `/post/like` `/post/delete` `/follow` `/logout` | 各 Servlet | フォームの送信先（処理後に元の画面へ戻る） |
| `/media/ファイル名` | MediaServlet | 画像・音声（見る権限がなければ 404） |
