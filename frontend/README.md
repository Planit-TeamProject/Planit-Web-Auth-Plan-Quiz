# Planit 프론트엔드 (김동호 파트)

회원가입/로그인/로그아웃과 퀴즈봇 화면입니다. React + TypeScript(Vite)로 만들었고,
백엔드 없이 Firebase(Authentication, Firestore)만 씁니다. 안드로이드 앱과 같은
Firebase 프로젝트라 앱/웹이 같은 계정과 데이터를 공유합니다.

스타일은 목업(`../src/main/resources/static/index.html`)에서 색·폰트·버튼·카드 규칙만
`src/styles/app.css`로 옮겨왔습니다. 팀원 웹과 합칠 때 갈아끼우면 됩니다.

## 실행

```bash
cd frontend
npm install
cp .env.example .env.local     # 아래 표 참고해서 Firebase 값 채우기
npm run dev                    # localhost:3000
```

`.env.local`이 비어 있으면 화면 위에 빨간 경고가 뜨고 로그인이 안 됩니다.
값은 Firebase 콘솔 → 프로젝트 설정 → 내 앱 → 웹 앱 → "SDK 설정 및 구성"에서 복사합니다.

빌드는 `npm run build`(`dist/`), 결과 확인은 `npm run preview` 입니다.

## 파일

| 파일 | 설명 |
| --- | --- |
| `src/firebase.ts` | Firebase 초기화. `auth`, `db` export |
| `src/auth/useCurrentUser.ts` | 로그인 상태 훅 |
| `src/api/auth.ts` | `signUp` / `signIn` / `signInWithGoogle` / `signOutUser`, 오류 메시지 한글화 |
| `src/api/quiz.ts` | 퀴즈 생성·제출·요약 (Firestore `quizzes`) |
| `src/api/quizQuestions.ts` | 문제 3개. 지금은 고정, OpenAI 붙이면 `generateQuestions`만 교체 |
| `src/pages/LoginPage.tsx` / `SignupPage.tsx` | 로그인 / 회원가입. 로그인에 "아이디 저장하기" 있음 |
| `src/pages/QuizPage.tsx` | 오늘의 퀴즈 (`/quiz`) |
| `src/App.tsx` | 라우팅 + 상단 nav(로그아웃 버튼) |
| `src/styles/app.css` | 목업에서 가져온 스타일 |
| `index.html` | Google Fonts(Space Grotesk / Inter / JetBrains Mono) 링크 |
| `public/study_plan.json` | 퀴즈 "오늘의 일과"에 쓰는 예시 데이터 |

## Firestore 구조

```
users/{uid}                            { name, email, createdAt }
quizzes/{quizId}                       { uid, subjectName, todayScope, quizDate,
                                         createdAt, questions[] }
quizzes/{quizId}/answers/{questionNo}  { selectedChoice, correct, answeredAt }
```

## 알아둘 것

- Vite라서 `import.meta.env.VITE_*`를 씁니다 (CRA의 `process.env.REACT_APP_*` 아닙니다).
  env 파일은 `frontend/.env.local` 입니다.
- 비밀번호 확인 일치와 8자 이상은 보내기 전에 화면에서 검사합니다. Firebase 자체 최소 길이는 6자입니다.
- 가입하거나 구글 로그인하면 Firestore `users/{uid}`에 `{ name, email, createdAt }`를 merge로 씁니다.
  필드 이름이 안드로이드 앱의 `users`와 맞는지 확인이 필요합니다.
- 퀴즈봇은 로그인해야 들어갑니다. 안 되어 있으면 로그인 안내가 뜹니다.
- 로그인 성공 후 어디로 갈지는 팀원 웹 담당이라, 지금은 "환영합니다"만 띄웁니다.

## Firebase 콘솔에서 해야 하는 것

1. 웹 앱 등록(`</>`) → config를 `.env.local`에 넣습니다.
2. Authentication → 로그인 방법 → 이메일/비밀번호, Google 둘 다 켭니다.
   (Google은 지원 이메일 지정, 승인된 도메인에 `localhost` 있는지 확인)
3. Firestore를 만들고 규칙을 작성합니다 — `users`, `quizzes`는 본인(uid) 문서만 읽기/쓰기.
4. 팀원을 프로젝트에 편집자로 초대합니다.
5. 안드로이드에서 만든 계정으로 웹 로그인 되는지 확인합니다.
