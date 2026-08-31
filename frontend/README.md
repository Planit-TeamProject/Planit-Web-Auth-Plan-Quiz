# Planit Frontend (김동호 담당 파트)

김동호 담당 기능을 React + TypeScript(Vite)로 구현한 것. 백엔드(Spring + MySQL) 없이
**Firebase** 로 동작한다 — 안드로이드 앱과 같은 Firebase 프로젝트라 앱/웹 유저가 같은
Authentication·Firestore 를 공유한다. 스타일은 목업(`src/main/resources/static/index.html`)의
디자인 토큰·버튼·카드 규칙만 추려 `src/styles/app.css` 로 적용했다(팀원 웹과 합칠 때 교체 가능).

- 회원가입 / 로그인 (이메일·비밀번호 + Google) / 로그아웃
- 퀴즈봇

## 실행

```bash
cd frontend
npm install
cp .env.example .env.local   # 그리고 실제 Firebase 값 채우기
npm run dev                  # http://localhost:3000
```

`.env.local` 이 비어 있으면 화면 상단에 빨간 경고가 뜨고 로그인/구글 로그인이 동작하지 않는다.
실제 값은 Firebase 콘솔 > 프로젝트 설정 > 내 앱 > 웹 앱 > "SDK 설정 및 구성" 에서 복사.

## 빌드

```bash
npm run build      # dist/
npm run preview
```

## 구조

| 파일 | 설명 |
| --- | --- |
| `src/firebase.ts` | Firebase 초기화. `auth` / `db`(Firestore) export |
| `src/auth/useCurrentUser.ts` | 로그인 상태 구독 훅 |
| `src/api/auth.ts` | `signUp` / `signIn` / `signInWithGoogle` / `signOutUser` + `authErrorMessage` |
| `src/api/quiz.ts` | 퀴즈 생성·제출·요약 (Firestore `quizzes`) |
| `src/api/quizQuestions.ts` | 문제 생성기 — 고정 예시 3문제 (OpenAI 연동 시 `generateQuestions` 만 교체) |
| `src/pages/LoginPage.tsx` / `SignupPage.tsx` | 화면 2 / 3. 로그인은 "아이디 저장하기" 지원 |
| `src/pages/QuizPage.tsx` | 화면 6. 오늘의 퀴즈 → `/quiz` |
| `src/App.tsx` | 라우팅 + 상단 nav(로그아웃 버튼 포함) |
| `src/styles/app.css` | 목업에서 추린 디자인 토큰·버튼·입력·카드 스타일 |
| `index.html` | Google Fonts(Space Grotesk / Inter / JetBrains Mono) 링크 |
| `public/study_plan.json` | 퀴즈 "오늘의 일과" 예시 데이터 |

## Firestore 데이터 모델

```
users/{uid}                            { name, email, createdAt }
quizzes/{quizId}                       { uid, subjectName, todayScope, quizDate,
                                         createdAt, questions[] }
quizzes/{quizId}/answers/{questionNo}  { selectedChoice, correct, answeredAt }
```

## 메모

- Vite 라서 `process.env.REACT_APP_*` 대신 `import.meta.env.VITE_*`, env 파일은 `frontend/.env.local`.
- 비밀번호 확인 일치(REQ-A-002)·최소 8자(REQ-A-004)는 전송 전 화면에서 검사 (Firebase 최소 6자).
- 회원가입/구글 로그인 시 Firestore `users/{uid}` 에 `{ name, email, createdAt }` merge.
  **필드명이 안드로이드 앱의 `users` 스키마와 맞는지 확인 필요.**
- 퀴즈봇은 로그인 필요(`useCurrentUser`). 미로그인 시 로그인 안내를 표시.
- 로그인 성공 후 이동할 메인 화면은 팀원 웹 담당 — 지금은 "환영합니다" 메시지만 표시.
- 학습계획입력(플랜 생성)은 다른 담당이라 이 프론트엔드에 없음.

## 코드 밖에서 해야 하는 일 (Firebase 콘솔)

1. 웹 앱 등록(`</>`) → config 를 `frontend/.env.local` 에 입력
2. Authentication > 로그인 방법 > **이메일/비밀번호** + **Google** 둘 다 사용 설정
   - Google: 프로젝트 지원 이메일 지정
   - Authentication > 설정 > 승인된 도메인에 `localhost` + 실제 배포 도메인
3. **Firestore** 사용 설정 + 보안 규칙: `users`·`quizzes` 는 본인(uid) 문서만
   읽기/쓰기 가능하도록 작성
4. 팀원을 프로젝트에 **편집자** 로 초대 (프로젝트 설정 > 사용자 및 권한)
5. 안드로이드 앱에서 만든 계정으로 웹 로그인 되는지 교차 확인
