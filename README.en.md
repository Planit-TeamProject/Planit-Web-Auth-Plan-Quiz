[🇰🇷 한국어](README.md) · **🇬🇧 English** · [🇯🇵 日本語](README.ja.md)

# Planit — Dongho Kim's Part

Handles signup/login (including Google), logout, and the quiz bot. Study-plan
input is owned by someone else, so it's not covered here.

Originally built with Spring + MySQL, then migrated to Firebase. It uses the
same Firebase project (`planit-ccfff`) as the Android app, so an account
created in the app logs in on the web as-is.

- Frontend: React + TypeScript (Vite)
- Auth: Firebase Authentication (email/password, Google)
- DB: Firestore (`users`, `quizzes`)
- Spring still exists but only serves static files — it doesn't touch the DB.

The mapping between requirement IDs and code locations is in
[`docs/requirements-mapping.md`](docs/requirements-mapping.md).

## Folders

```
frontend/                          ← actual code
  src/firebase.ts                  Firebase init (auth, db)
  src/auth/useCurrentUser.ts       login-state hook
  src/api/
    auth.ts                        signup / login / Google login / logout
    quiz.ts                        quiz generation / grading / summary
    quizQuestions.ts               3 fixed questions for now, swap for OpenAI later
  src/pages/
    LoginPage.tsx  SignupPage.tsx  QuizPage.tsx
  src/styles/app.css               colors/fonts/buttons pulled from the mockup
src/main/java/com/planit/PlanitApplication.java   static serving only
src/main/resources/static/index.html              old mockup (for reference)
```

## Running it

```bash
cd frontend
npm install
cp .env.example .env.local     # fill in the Firebase config values
npm run dev                    # localhost:3000
```

Things you need to do in the Firebase console (register the web app, turn on
sign-in methods, Firestore rules) are written up in `frontend/README.md`.

To run Spring, use `./gradlew bootRun` (localhost:8080, mockup only).
No DB setup needed.

## Firestore structure

```
users/{uid}                            { name, email, createdAt }
quizzes/{quizId}                       { uid, subjectName, todayScope, quizDate,
                                         createdAt, questions[] }
quizzes/{quizId}/answers/{questionNo}  { selectedChoice, correct, answeredAt }
```

## Not done yet

- Quiz questions are 3 fixed ones. OpenAI integration is Jimin Park's job —
  once the approach is decided, only `generateQuestions` in `quizQuestions.ts`
  needs to change.
- Wiring quiz results into the checklist / plan re-adjustment isn't done yet.
  It needs to hook into Siwoo Yu / Gyeongtae Kim / Jimin Park's features.
