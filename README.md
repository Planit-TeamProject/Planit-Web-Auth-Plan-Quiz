**🇰🇷 한국어** · [🇬🇧 English](README.en.md) · [🇯🇵 日本語](README.ja.md)

---

# Planit — 김동호 파트

회원가입 / 로그인(구글 포함) · 로그아웃 · 퀴즈봇을 담당합니다.
학습계획입력은 다른 사람 담당이라 여기 없습니다.

원래 Spring + MySQL로 짜다가 Firebase로 옮겼습니다. 안드로이드 앱과 같은 Firebase
프로젝트(`planit-ccfff`)를 쓰기 때문에, 앱에서 가입한 계정으로 웹에서도 그대로 로그인됩니다.

요구사항 ID와 코드 위치 매핑은 [`docs/requirements-mapping.md`](docs/requirements-mapping.md)에
있습니다.

## 기술 스택

| 구분 | 선택 |
|---|---|
| 프론트 | React + TypeScript (Vite) |
| 인증 | Firebase Authentication (이메일/비번, 구글) |
| DB | Firestore (`users`, `quizzes`) |
| 백엔드 | Spring Boot — 정적 파일 서빙만, DB는 쓰지 않음 |

## 담당 기능 상세

### 1. 회원가입 / 로그인 — REQ-A-001~015

| 파일 | 역할 |
|---|---|
| `pages/SignupPage.tsx` | 이름/이메일/비밀번호 입력, 비밀번호 확인 일치·형식 검증(REQ-A-002~004), 가입 성공 시 로그인 화면 이동(REQ-A-007) |
| `pages/LoginPage.tsx` | 이메일/비밀번호 로그인, 구글 로그인 버튼, "아이디 저장하기" 옵트인 |
| `api/auth.ts` `signUp()` | Firebase Auth 계정 생성 + 표시 이름 저장 + Firestore `users/{uid}` 문서 생성 |
| `api/auth.ts` `signIn()` | 이메일/비밀번호 로그인 |
| `api/auth.ts` `signInWithGoogle()` / `consumeRedirectResult()` | 구글 로그인(REQ-A-014). 팝업이 막히면 리다이렉트 방식으로 자동 전환 |
| `api/auth.ts` `authErrorMessage()` | Firebase 에러 코드(`auth/invalid-email` 등)를 화면 문구용 번역 키로 매핑(REQ-NF-015) |
| `auth/useCurrentUser.ts` | 로그인 상태를 구독하는 훅 — 세션 유지(REQ-A-011) |
| `firebase.ts` | Firebase 프로젝트 초기화, `.env.local` 설정 누락 감지 |

- 비밀번호 저장/암호화(REQ-NF-009), 이메일 유일성(REQ-NF-010), 세션 저장(REQ-NF-011), 반복 로그인 실패 차단(REQ-NF-013)은
  전부 Firebase Authentication이 처리 — 별도 코드 없음.

### 2. 로그아웃 — REQ-A-012

- `App.tsx`의 `TopNav` → `handleLogout()` → `api/auth.ts` `signOutUser()` 호출 후 로그인 화면으로 이동.

### 3. 퀴즈봇 — REQ-Q-001~006

| 파일 | 역할 |
|---|---|
| `pages/QuizPage.tsx` | 퀴즈 시작 버튼, 문제 제출 UI, 결과 요약 표시 |
| `api/quiz.ts` `startQuiz()` | 오늘 학습 범위로 BASIC 2 + APPLIED 1 문제를 만들어 Firestore `quizzes`에 저장(REQ-Q-001~003) |
| `api/quiz.ts` `submitAnswer()` | 문제 하나 제출 → 즉시 채점 → `quizzes/{id}/answers/{questionNo}`에 저장(REQ-Q-004~005) |
| `api/quiz.ts` `getQuizSummary()` | 제출된 답안을 모아 맞힌 개수 집계(REQ-Q-006) |
| `api/quizQuestions.ts` `generateQuestions()` | 문제 생성 로직. 지금은 과목/범위와 무관하게 고정 예시 3문제 반환 — OpenAI 연동(박지민) 전까지 임시 |

요구사항 ID 하나하나까지 전부 맞춰본 표는 [`docs/requirements-mapping.md`](docs/requirements-mapping.md)에 있습니다.

## 폴더 구조

```
frontend/                        ← 실제 코드
├── src/
│   ├── firebase.ts              Firebase 초기화 (auth, db)
│   ├── auth/useCurrentUser.ts   로그인 상태 훅
│   ├── api/
│   │   ├── auth.ts              가입 / 로그인 / 구글 로그인 / 로그아웃
│   │   ├── quiz.ts              퀴즈 생성·채점·요약
│   │   └── quizQuestions.ts     문제 3개 (지금은 고정, 나중에 OpenAI로 교체)
│   ├── pages/                   LoginPage / SignupPage / QuizPage
│   └── styles/app.css           목업에서 가져온 색·폰트·버튼 스타일
src/main/java/com/planit/
└── PlanitApplication.java       정적 서빙만
src/main/resources/static/
└── index.html                   옛날 목업 (참고용)
```

## 실행 방법

```bash
cd frontend
npm install
cp .env.example .env.local     # Firebase config 값 채우기
npm run dev                    # localhost:3000
```

> Firebase 콘솔에서 해야 하는 것(웹 앱 등록, 로그인 방법 켜기, Firestore 규칙)은
> `frontend/README.md`에 적어 두었습니다.

Spring을 띄우려면 `./gradlew bootRun` 입니다 (localhost:8080, 목업만 나옵니다). DB 설정은 필요 없습니다.

## Firestore 구조

```
users/{uid}                            { name, email, createdAt }
quizzes/{quizId}                       { uid, subjectName, todayScope, quizDate,
                                          createdAt, questions[] }
quizzes/{quizId}/answers/{questionNo}  { selectedChoice, correct, answeredAt }
```

## 아직 안 한 것

- **퀴즈 문제 생성**: 지금은 고정 3문제입니다. OpenAI 연동은 박지민 담당이고, 방식이 정해지면
  `quizQuestions.ts`의 `generateQuestions`만 바꾸면 됩니다.
- **퀴즈 결과 연동**: 체크리스트 / 계획 재조정과 연결하는 부분은 아직입니다.
  유시우·김경태·박지민 쪽 기능과 물려야 합니다.
