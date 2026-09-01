[🇰🇷 한국어](README.md) · [🇬🇧 English](README.en.md) · **🇯🇵 日本語**

---

# Planit — キム・ドンホ担当パート

会員登録 / ログイン(Google含む) · ログアウト · クイズボットを担当しています。
学習計画入力は別の人の担当なのでここには含まれません。

もともとSpring + MySQLで作っていましたが、Firebaseに移行しました。Androidアプリと同じ
Firebaseプロジェクト(`planit-ccfff`)を使っているため、アプリで登録したアカウントで
そのままWebにもログインできます。

要件IDと実コードの対応は [`docs/requirements-mapping.md`](docs/requirements-mapping.md)
にまとめてあります。

## 技術スタック

| 区分 | 選択 |
|---|---|
| フロント | React + TypeScript (Vite) |
| 認証 | Firebase Authentication (メール/パスワード、Google) |
| DB | Firestore (`users`, `quizzes`) |
| バックエンド | Spring Boot — 静的ファイルの配信のみ、DBは使わない |

## 担当機能の詳細

### 1. 会員登録 / ログイン — REQ-A-001~015

| ファイル | 役割 |
|---|---|
| `pages/SignupPage.tsx` | 名前/メール/パスワード入力、パスワード確認一致・形式検証(REQ-A-002~004)、登録成功後にログイン画面へ遷移(REQ-A-007) |
| `pages/LoginPage.tsx` | メール/パスワードログイン、Googleログインボタン、「メール記憶」オプトイン |
| `api/auth.ts` `signUp()` | Firebase Authアカウント作成 + 表示名保存 + Firestore `users/{uid}` ドキュメント作成 |
| `api/auth.ts` `signIn()` | メール/パスワードログイン |
| `api/auth.ts` `signInWithGoogle()` / `consumeRedirectResult()` | Googleログイン(REQ-A-014)。ポップアップがブロックされた場合は自動でリダイレクト方式に切り替え |
| `api/auth.ts` `authErrorMessage()` | Firebaseのエラーコード(`auth/invalid-email`等)を画面表示用の翻訳キーにマッピング(REQ-NF-015) |
| `auth/useCurrentUser.ts` | ログイン状態を購読するフック — セッション維持(REQ-A-011) |
| `firebase.ts` | Firebaseプロジェクトの初期化、`.env.local`設定漏れの検知 |

- パスワードの保存/暗号化(REQ-NF-009)、メールの一意性(REQ-NF-010)、セッション保存(REQ-NF-011)、
  ログイン連続失敗のロック(REQ-NF-013)はすべてFirebase Authenticationが処理 — 独自コードなし。

### 2. ログアウト — REQ-A-012

- `App.tsx`の`TopNav` → `handleLogout()` → `api/auth.ts`の`signOutUser()`を呼び出した後、ログイン画面へ遷移。

### 3. クイズボット — REQ-Q-001~006

| ファイル | 役割 |
|---|---|
| `pages/QuizPage.tsx` | クイズ開始ボタン、回答提出UI、結果要約表示 |
| `api/quiz.ts` `startQuiz()` | 今日の学習範囲からBASIC2問+APPLIED1問を生成し、Firestoreの`quizzes`に保存(REQ-Q-001~003) |
| `api/quiz.ts` `submitAnswer()` | 問題を1つ提出 → 即座に採点 → `quizzes/{id}/answers/{questionNo}`に保存(REQ-Q-004~005) |
| `api/quiz.ts` `getQuizSummary()` | 提出済みの回答を集計して正解数を要約(REQ-Q-006) |
| `api/quizQuestions.ts` `generateQuestions()` | 問題生成ロジック。現在は科目/範囲に関係なく固定の例題3問を返す — OpenAI連携(パク・ジミン)までの暫定実装 |

要件IDひとつひとつまで対応させた表は
[`docs/requirements-mapping.md`](docs/requirements-mapping.md)にあります。

## フォルダ構成

```
frontend/                        ← 実コード
├── src/
│   ├── firebase.ts              Firebase初期化 (auth, db)
│   ├── auth/useCurrentUser.ts   ログイン状態フック
│   ├── api/
│   │   ├── auth.ts              登録 / ログイン / Googleログイン / ログアウト
│   │   ├── quiz.ts              クイズ生成・採点・要約
│   │   └── quizQuestions.ts     問題3問(今は固定、後でOpenAIに置き換え予定)
│   ├── pages/                   LoginPage / SignupPage / QuizPage
│   └── styles/app.css           モックアップから流用した色・フォント・ボタンのスタイル
src/main/java/com/planit/
└── PlanitApplication.java       静的配信のみ
src/main/resources/static/
└── index.html                   旧モックアップ(参考用)
```

## 実行方法

```bash
cd frontend
npm install
cp .env.example .env.local     # Firebaseの設定値を入力
npm run dev                    # localhost:3000
```

> Firebaseコンソール側でやること(Webアプリ登録、ログイン方法の有効化、Firestoreルール)は
> `frontend/README.md`に書いてあります。

Springを起動するには `./gradlew bootRun`(localhost:8080、モックアップのみ表示)。DB設定は不要です。

## Firestore構造

```
users/{uid}                            { name, email, createdAt }
quizzes/{quizId}                       { uid, subjectName, todayScope, quizDate,
                                          createdAt, questions[] }
quizzes/{quizId}/answers/{questionNo}  { selectedChoice, correct, answeredAt }
```

## まだやっていないこと

- **クイズ問題生成**: 今は固定3問です。OpenAI連携はパク・ジミン担当で、方式が決まれば
  `quizQuestions.ts`の`generateQuestions`だけ差し替えれば済みます。
- **クイズ結果の連携**: チェックリスト・計画再調整との連携は未着手です。
  ユ・シウ、キム・ギョンテ、パク・ジミンの担当機能と連動させる必要があります。
