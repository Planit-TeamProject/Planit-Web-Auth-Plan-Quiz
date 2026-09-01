# Planit — 김동호 파트

회원가입/로그인(구글 포함), 로그아웃, 퀴즈봇을 담당합니다. 학습계획입력은 다른 사람
담당이라 여기 없습니다.

원래 Spring + MySQL로 짜다가 Firebase로 옮겼습니다. 안드로이드 앱과 같은 Firebase
프로젝트(`planit-ccfff`)를 쓰기 때문에, 앱에서 가입한 계정으로 웹에서도 그대로 로그인됩니다.

- 프론트: React + TypeScript (Vite)
- 인증: Firebase Authentication (이메일/비번, 구글)
- DB: Firestore (`users`, `quizzes`)
- Spring은 남아 있지만 정적 파일 서빙만 하고, DB는 쓰지 않습니다.

요구사항 ID와 코드 위치 매핑은 [`docs/requirements-mapping.md`](docs/requirements-mapping.md)에
있습니다.

## 폴더

```
frontend/                          ← 실제 코드
  src/firebase.ts                  Firebase 초기화 (auth, db)
  src/auth/useCurrentUser.ts       로그인 상태 훅
  src/api/
    auth.ts                        가입 / 로그인 / 구글 로그인 / 로그아웃
    quiz.ts                        퀴즈 생성·채점·요약
    quizQuestions.ts               문제 3개 (지금은 고정, 나중에 OpenAI로 교체)
  src/pages/
    LoginPage.tsx  SignupPage.tsx  QuizPage.tsx
  src/styles/app.css               목업에서 가져온 색·폰트·버튼 스타일
src/main/java/com/planit/PlanitApplication.java   정적 서빙만
src/main/resources/static/index.html              옛날 목업 (참고용)
```

## 실행

```bash
cd frontend
npm install
cp .env.example .env.local     # Firebase config 값 채우기
npm run dev                    # localhost:3000
```

Firebase 콘솔에서 해야 하는 것(웹 앱 등록, 로그인 방법 켜기, Firestore 규칙)은
`frontend/README.md`에 적어 두었습니다.

Spring을 띄우려면 `./gradlew bootRun` 입니다 (localhost:8080, 목업만 나옵니다).
DB 설정은 필요 없습니다.

## Firestore 구조

```
users/{uid}                            { name, email, createdAt }
quizzes/{quizId}                       { uid, subjectName, todayScope, quizDate,
                                         createdAt, questions[] }
quizzes/{quizId}/answers/{questionNo}  { selectedChoice, correct, answeredAt }
```

## 아직 안 한 것

- 퀴즈 문제가 고정 3개입니다. OpenAI 연동은 박지민 담당이고, 방식이 정해지면
  `quizQuestions.ts`의 `generateQuestions`만 바꾸면 됩니다.
- 퀴즈 결과를 체크리스트/계획 재조정과 연결하는 부분은 아직입니다. 유시우·김경태·박지민
  쪽 기능과 물려야 합니다.
