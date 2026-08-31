# Planit — キム・ドンホ担当分（方式B）

*[한국어](README.md) · [English](README.en.md)*

新規登録／ログイン（Google含む）、ログアウト、退会、クイズボットを担当します。学習計画入力は
別の担当なので、このリポジトリにはありません。

## 全体の流れ（方式B）

```
[ブラウザ]  login.html
   1. Firebase JS でメール/パスワード（またはGoogle）ログイン
   2. user.getIdToken() で IDトークンを取得
   3. POST /api/auth/firebase-login { idToken }  を Spring へ送信
[Spring]
   4. Firebase Admin SDK でトークンを検証 → セッションに uid/email を保存
[以降のすべてのページ]  quiz.html …
   セッションクッキーだけで /api/* を呼ぶ
   Firestore の読み書きは Spring が Admin SDK で処理する
```

**要点：ブラウザは Firestore に直接アクセスしません。** ログインだけを JS で行い、
データは必ず Spring を経由します。（`login.html` は `firebase-auth` のみを読み込み、
`firebase-firestore` は読み込みません。）

- ブラウザ：素の HTML + バニラ JS。React／npm は使いません。
- サーバー：Spring Boot 3.3.x + `firebase-admin`。MySQL/JPA なし。
- 認証：Firebase Authentication（メール/パスワード、Google）
- データ：Cloud Firestore（`quizzes`）、アクセスはサーバーのみ

## 構成

```
src/main/java/com/planit/
  PlanitApplication.java
  config/
    FirebaseConfig.java          サービスアカウントキーで Admin SDK を初期化
    WebConfig.java               /api/quizzes/** にログインインターセプター
  auth/
    AuthController.java          POST /api/auth/firebase-login, /logout, /withdraw, GET /me
    AuthInterceptor.java         セッションが無ければ 401
    SessionUser.java
  quiz/
    QuizController.java          GET /today-plan, POST /, POST /{id}/answers/{no}, GET /{id}/summary
    QuizService.java             Firestore（Admin SDK）で生成・採点・集計
    QuizQuestionGenerator.java / MockQuizQuestionGenerator.java   固定サンプル3問
  global/
    ApiException.java / GlobalExceptionHandler.java
src/main/resources/
  static/login.html   ログイン/新規登録（Firebase Auth compat CDN）
  static/quiz.html    クイズボット（Firebase SDK 未使用、/api/* のみ呼ぶ）
  static/app.css      共通スタイル（モックアップのパレット）
  static/study_plan.json   クイズの「今日の予定」用サンプルデータ（サーバーが読む）
  static/index.html   旧・7画面モックアップ — 参考用（動作しません）
```

## 実行方法

### 1. サービスアカウントキーを置く（リーダーが発行して共有）

Firebase コンソール → プロジェクトの設定（⚙️）→ **サービス アカウント** → **新しい秘密鍵の生成**
→ 受け取った JSON を `src/main/resources/` に置きます。ファイル名はどちらでも可：

- `firebase-service-account.json` にリネームする、または
- コンソールで受け取った元の名前のまま（例：`planit-ccfff-firebase-adminsdk-xxxx.json`）

`FirebaseConfig` が両方を自動で探します。絶対パスを指定する場合は環境変数
`FIREBASE_CREDENTIALS=/path/to/key.json` でも可。

このキーは本物の秘密鍵です。**絶対にコミットしないでください**（`.gitignore` に
`*-firebase-adminsdk-*.json` を登録済み）。Notion／ドライブでのみ共有します。
`login.html` 内の `firebaseConfig`（apiKey など）とはまったく別物です — そちらは公開されて問題ありません。

### 2. サーバーの起動

```bash
./gradlew bootRun        # http://localhost:8080
```

`http://localhost:8080/login.html` でログインすると、自動的に `quiz.html` へ移動します。

キーファイルが無くてもサーバーは起動しますが、`/api/auth/firebase-login` は 503 を返します。

## Firestore の構造

```
quizzes/{quizId}                       { uid, subjectName, todayScope, quizDate,
                                         createdAt, questions[] }
quizzes/{quizId}/answers/{questionNo}  { selectedChoice, correct, answeredAt }
```

`questions[]` には正解番号・解説まで保存しますが、出題用レスポンス（`POST /api/quizzes`）では
それらを除いて返します。

## Firebase コンソールの設定

1. Authentication → ログイン方法 → **メール/パスワード** と **Google** の両方を有効化
   （Google はサポートメールを指定、承認済みドメインに `localhost` があるか確認）
2. Firestore データベースを作成
3. Firestore のルール：サーバーは Admin SDK でルールを回避しますが、クライアントからの
   直接アクセスを防ぐためルールは有効のまま（デフォルトのロック状態でOK）にしておきます。
4. サービスアカウントキーを発行（上記「実行方法」1）

## 退会

`quiz.html` の「회원 탈퇴」ボタン → `POST /api/auth/withdraw`（ログインセッションが必要）。
サーバーが Admin SDK で、(1) そのユーザーの `quizzes` データを削除、(2) Firebase Auth の
アカウントを削除、(3) セッションを無効化します。ブラウザ側では `localStorage` に保存した
メールアドレスも消します。

`users/{uid}` ドキュメントや他ドメインのデータ（`study_plan_items` など）には**触れません** —
方式B のウェブは `users` に書き込まず、このコレクションはチーム共有だからです。
アカウントデータの完全な後片付けはチームでの調整が必要です（下記参照）。

## まだやっていないこと

- クイズの問題は固定の3問です。OpenAI 連携はパク・ジミン担当で、方式が決まったら
  `MockQuizQuestionGenerator` の代わりに新しい `QuizQuestionGenerator` 実装を `@Primary` で登録します。
- クイズ結果をチェックリスト／再計画機能とつなぐ部分はまだです。
- 退会時に `users/{uid}` や他ドメインのデータまで削除する方法（例：Auth の `onDelete`
  Cloud Function ひとつで全部消す）はチームでの検討が必要です。
