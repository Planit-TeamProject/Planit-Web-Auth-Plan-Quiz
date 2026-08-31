# Planit — Kim Dongho's part (Approach B)

*[한국어](README.md) · [日本語](README.ja.md)*

Covers sign-up / login (including Google), logout, account withdrawal, and the quiz bot.
Study-plan input is someone else's area and isn't in this repo.

- Team's final integrated schema: [`docs/schema.sql`](docs/schema.sql) (MySQL-based)
- Requirement-ID ↔ code mapping + schema ↔ Approach B table: [`docs/requirements-mapping.md`](docs/requirements-mapping.md)
- This repo implements Kim Dongho's part in Firestore instead of MySQL — **Approach B** (see the mapping table).

## How it fits together (Approach B)

```
[Browser]  login.html
   1. Sign in with Firebase JS (email/password or Google)
   2. Get an ID token via user.getIdToken()
   3. POST /api/auth/firebase-login { idToken }  to Spring
[Spring]
   4. Verify the token with the Firebase Admin SDK → store uid/email in the session
[Every page after that]  quiz.html …
   Call /api/* with the session cookie only
   Spring does all Firestore reads/writes through the Admin SDK
```

**The key rule: the browser never touches Firestore directly.** Only login runs in JS;
all data goes through Spring. (`login.html` loads `firebase-auth` only, never
`firebase-firestore`.)

- Browser: plain HTML + vanilla JS. No React, no npm.
- Server: Spring Boot 3.3.x + `firebase-admin`. No MySQL/JPA.
- Auth: Firebase Authentication (email/password, Google)
- Data: Cloud Firestore (`quizzes`), server access only

## Layout

```
src/main/java/com/planit/
  PlanitApplication.java
  config/
    FirebaseConfig.java          Initializes the Admin SDK from the service-account key
    WebConfig.java               Login interceptor on /api/quizzes/**
  auth/
    AuthController.java          POST /api/auth/firebase-login, /logout, /withdraw, GET /me
    AuthInterceptor.java         401 if there's no session
    SessionUser.java
  quiz/
    QuizController.java          GET /today-plan, POST /, POST /{id}/answers/{no}, GET /{id}/summary
    QuizService.java             Create / grade / summarize via Firestore (Admin SDK)
    QuizQuestionGenerator.java / MockQuizQuestionGenerator.java   fixed sample of 3 questions
  global/
    ApiException.java / GlobalExceptionHandler.java
src/main/resources/
  static/login.html   Login / sign-up (Firebase Auth compat CDN)
  static/quiz.html    Quiz bot (no Firebase SDK; calls /api/* only)
  static/app.css      Shared styles (mockup palette)
  static/study_plan.json   Sample data for the quiz's "today's plan" (read by the server)
  static/index.html   Old 7-screen mockup — reference only (doesn't work)
```

## Running it

### 1. Add the service-account key (the lead generates and shares it)

Firebase console → Project settings (⚙️) → **Service accounts** → **Generate new private key**
→ put the JSON under `src/main/resources/`. Either filename works:

- rename it to `firebase-service-account.json`, or
- keep the original name from the console (e.g. `planit-ccfff-firebase-adminsdk-xxxx.json`)

`FirebaseConfig` finds both. To point at an absolute path, set the env var
`FIREBASE_CREDENTIALS=/path/to/key.json`.

This key is a real secret. **Never commit it** (`*-firebase-adminsdk-*.json` is in `.gitignore`);
share it only over Notion/Drive. It's completely different from the `firebaseConfig`
(apiKey etc.) in `login.html` — that one is fine to be public.

### 2. Start the server

```bash
./gradlew bootRun        # http://localhost:8080
```

Log in at `http://localhost:8080/login.html` → you're taken to `quiz.html` automatically.

If the key file is missing the server still starts, but `/api/auth/firebase-login` returns 503.

## Firestore shape

```
quizzes/{quizId}                       { uid, subjectName, todayScope, quizDate,
                                         createdAt, questions[] }
quizzes/{quizId}/answers/{questionNo}  { selectedChoice, correct, answeredAt }
```

`questions[]` stores the answer number and explanation too; the play response
(`POST /api/quizzes`) strips those out.

## Firebase console setup

1. Authentication → Sign-in method → enable both **Email/Password** and **Google**
   (set a support email for Google; check `localhost` is in the authorized domains)
2. Create a Firestore database
3. Firestore rules: the server bypasses them with the Admin SDK, but leave the rules on
   (locked by default is fine) to block direct client access.
4. Generate the service-account key (step 1 above)

## Account withdrawal

The "회원 탈퇴" button in `quiz.html` → `POST /api/auth/withdraw` (needs a login session).
The server, via the Admin SDK: (1) deletes that user's `quizzes` data, (2) deletes the
Firebase Auth account, (3) invalidates the session. The browser also clears the saved
email from `localStorage`.

It does **not** touch the `users/{uid}` document or other domains' data
(`study_plan_items`, etc.) — Approach B's web never writes to `users`, and that collection
is shared across the team. Full account-data cleanup needs a team decision (see below).

## Not done yet

- The quiz has a fixed set of 3 questions. OpenAI integration is Park Jimin's area; once
  it's decided, register a new `QuizQuestionGenerator` implementation as `@Primary` in place
  of `MockQuizQuestionGenerator`.
- Wiring quiz results into the checklist / re-planning features isn't done yet.
- Cleaning up `users/{uid}` and other-domain data on withdrawal (e.g. one Auth `onDelete`
  Cloud Function that wipes everything) needs a team decision.
