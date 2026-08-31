[🇰🇷 한국어](README.md) · [🇬🇧 English](README.en.md) · **🇯🇵 日本語**

# Planit — キム・ドンホ担当パート

会員登録/ログイン(Google含む)、ログアウト、クイズボットを担当しています。学習計画入力は
別の人の担当なのでここには含まれません。

もともとSpring + MySQLで作っていましたが、Firebaseに移行しました。Androidアプリと同じ
Firebaseプロジェクト(`planit-ccfff`)を使っているため、アプリで登録したアカウントで
そのままWebにもログインできます。

- フロント: React + TypeScript (Vite)
- 認証: Firebase Authentication (メール/パスワード、Google)
- DB: Firestore (`users`, `quizzes`)
- Springは残っていますが静的ファイルの配信のみで、DBは使っていません。

要件IDと実コードの対応は [`docs/requirements-mapping.md`](docs/requirements-mapping.md)
にまとめてあります。

## フォルダ構成

```
frontend/                          ← 実コード
  src/firebase.ts                  Firebase初期化 (auth, db)
  src/auth/useCurrentUser.ts       ログイン状態フック
  src/api/
    auth.ts                        登録 / ログイン / Googleログイン / ログアウト
    quiz.ts                        クイズ生成・採点・要約
    quizQuestions.ts               問題3問(今は固定、後でOpenAIに置き換え予定)
  src/pages/
    LoginPage.tsx  SignupPage.tsx  QuizPage.tsx
  src/styles/app.css               モックアップから流用した色・フォント・ボタンのスタイル
src/main/java/com/planit/PlanitApplication.java   静的配信のみ
src/main/resources/static/index.html              旧モックアップ(参考用)
```

## 実行方法

```bash
cd frontend
npm install
cp .env.example .env.local     # Firebaseの設定値を入力
npm run dev                    # localhost:3000
```

Firebaseコンソール側でやること(Webアプリ登録、ログイン方法の有効化、Firestoreルール)は
`frontend/README.md`に書いてあります。

Springを起動するには `./gradlew bootRun`(localhost:8080、モックアップのみ表示)。
DB設定は不要です。

## Firestore構造

```
users/{uid}                            { name, email, createdAt }
quizzes/{quizId}                       { uid, subjectName, todayScope, quizDate,
                                         createdAt, questions[] }
quizzes/{quizId}/answers/{questionNo}  { selectedChoice, correct, answeredAt }
```

## まだやっていないこと

- クイズ問題は固定3問です。OpenAI連携はパク・ジミン担当で、方式が決まれば
  `quizQuestions.ts`の`generateQuestions`だけ差し替えれば済みます。
- クイズ結果をチェックリスト・計画再調整と連携する部分は未着手です。
  ユ・シウ、キム・ギョンテ、パク・ジミンの担当機能と連動させる必要があります。
