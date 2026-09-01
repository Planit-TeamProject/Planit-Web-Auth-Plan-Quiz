[🇰🇷 한국어](README.md) · **🇬🇧 English** · [🇯🇵 日本語](README.ja.md)

---

# Planit — Dongho Kim's Part

Signup / login (including Google) · logout · quiz bot.
Study-plan input is owned by someone else, so it's not covered here.

Originally built with Spring + MySQL, then migrated to Firebase. It uses the
same Firebase project (`planit-ccfff`) as the Android app, so an account
created in the app logs in on the web as-is.

The mapping between requirement IDs and code locations is in
[`docs/requirements-mapping.md`](docs/requirements-mapping.md).

## Tech stack

| Category | Choice |
|---|---|
| Frontend | React + TypeScript (Vite) |
| Auth | Firebase Authentication (email/password, Google) |
| DB | Firestore (`users`, `quizzes`) |
| Backend | Spring Boot — static file serving only, doesn't touch the DB |

## What I built — details

### 1. Signup / login — REQ-A-001~015

| File | Role |
|---|---|
| `pages/SignupPage.tsx` | Name/email/password input, password-confirm match & format validation (REQ-A-002~004), redirect to login on success (REQ-A-007) |
| `pages/LoginPage.tsx` | Email/password login, Google login button, "remember email" opt-in |
| `api/auth.ts` `signUp()` | Creates the Firebase Auth account + sets the display name + creates the Firestore `users/{uid}` doc |
| `api/auth.ts` `signIn()` | Email/password login |
| `api/auth.ts` `signInWithGoogle()` / `consumeRedirectResult()` | Google login (REQ-A-014). Falls back to redirect automatically if the popup is blocked |
| `api/auth.ts` `authErrorMessage()` | Maps Firebase error codes (`auth/invalid-email`, etc.) to translation keys for on-screen messages (REQ-NF-015) |
| `auth/useCurrentUser.ts` | Hook that subscribes to login state — session persistence (REQ-A-011) |
| `firebase.ts` | Firebase project init, detects missing `.env.local` config |

- Password storage/encryption (REQ-NF-009), email uniqueness (REQ-NF-010), session storage
  (REQ-NF-011), and repeated-login-failure lockout (REQ-NF-013) are all handled by Firebase
  Authentication — no custom code needed.

### 2. Logout — REQ-A-012

- `TopNav` in `App.tsx` → `handleLogout()` → calls `signOutUser()` in `api/auth.ts`, then redirects to login.

### 3. Quiz bot — REQ-Q-001~006

| File | Role |
|---|---|
| `pages/QuizPage.tsx` | Start-quiz button, answer-submission UI, result summary display |
| `api/quiz.ts` `startQuiz()` | Builds 2 BASIC + 1 APPLIED question from today's scope and saves it to Firestore `quizzes` (REQ-Q-001~003) |
| `api/quiz.ts` `submitAnswer()` | Submits one answer → grades it instantly → saves to `quizzes/{id}/answers/{questionNo}` (REQ-Q-004~005) |
| `api/quiz.ts` `getQuizSummary()` | Aggregates submitted answers into a correct-count summary (REQ-Q-006) |
| `api/quizQuestions.ts` `generateQuestions()` | Question-generation logic. Currently returns the same fixed 3 questions regardless of subject/scope — a stand-in until OpenAI integration (Jimin Park) lands |

The full requirement-ID-by-requirement-ID mapping is in
[`docs/requirements-mapping.md`](docs/requirements-mapping.md).

## Folders

```
frontend/                        ← actual code
├── src/
│   ├── firebase.ts              Firebase init (auth, db)
│   ├── auth/useCurrentUser.ts   login-state hook
│   ├── api/
│   │   ├── auth.ts              signup / login / Google login / logout
│   │   ├── quiz.ts              quiz generation / grading / summary
│   │   └── quizQuestions.ts     3 fixed questions for now, swap for OpenAI later
│   ├── pages/                   LoginPage / SignupPage / QuizPage
│   └── styles/app.css           colors/fonts/buttons pulled from the mockup
src/main/java/com/planit/
└── PlanitApplication.java       static serving only
src/main/resources/static/
└── index.html                   old mockup (for reference)
```

## Running it

```bash
cd frontend
npm install
cp .env.example .env.local     # fill in the Firebase config values
npm run dev                    # localhost:3000
```

> Things you need to do in the Firebase console (register the web app, turn on
> sign-in methods, Firestore rules) are written up in `frontend/README.md`.

To run Spring, use `./gradlew bootRun` (localhost:8080, mockup only). No DB setup needed.

## Firestore structure

```
users/{uid}                            { name, email, createdAt }
quizzes/{quizId}                       { uid, subjectName, todayScope, quizDate,
                                          createdAt, questions[] }
quizzes/{quizId}/answers/{questionNo}  { selectedChoice, correct, answeredAt }
```

## Not done yet

- **Quiz question generation**: 3 fixed questions for now. OpenAI integration is
  Jimin Park's job — once the approach is decided, only `generateQuestions` in
  `quizQuestions.ts` needs to change.
- **Wiring quiz results**: connecting to the checklist / plan re-adjustment isn't
  done yet. It needs to hook into Siwoo Yu / Gyeongtae Kim / Jimin Park's features.
