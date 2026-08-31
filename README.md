# Planit — 김동호 파트 (방식 B)

*[English](README.en.md) · [日本語](README.ja.md)*

회원가입/로그인(구글 포함), 로그아웃, 퀴즈봇을 담당합니다. 학습계획입력은 다른 사람
담당이라 여기 없습니다.

## 구조 (방식 B)

```
[브라우저]  login.html
   1. Firebase JS 로 이메일/비번(또는 구글) 로그인
   2. user.getIdToken() 으로 ID 토큰 받음
   3. POST /api/auth/firebase-login { idToken }  로 Spring 에 전송
[Spring]
   4. Firebase Admin SDK 로 토큰 검증 → 세션에 uid/email 저장
[이후 모든 페이지]  quiz.html …
   세션 쿠키만으로 /api/* 호출
   Firestore 읽기/쓰기는 Spring 이 Admin SDK 로 처리
```

**핵심: 브라우저는 Firestore 를 직접 건드리지 않습니다.** 로그인만 JS 로 하고,
데이터는 항상 Spring 을 거칩니다. (`login.html` 은 `firebase-auth` 만 로드하고
`firebase-firestore` 는 로드하지 않습니다.)

- 브라우저: 순수 HTML + 바닐라 JS. React/npm 안 씁니다.
- 서버: Spring Boot 3.3.x + `firebase-admin`. MySQL/JPA 없음.
- 인증: Firebase Authentication (이메일/비번, 구글)
- 데이터: Cloud Firestore (`quizzes`), 접근은 서버만

## 폴더

```
src/main/java/com/planit/
  PlanitApplication.java
  config/
    FirebaseConfig.java          서비스 계정 키로 Admin SDK 초기화
    WebConfig.java               /api/quizzes/** 에 로그인 인터셉터
  auth/
    AuthController.java          POST /api/auth/firebase-login, /logout, /withdraw, GET /me
    AuthInterceptor.java         세션 없으면 401
    SessionUser.java
  quiz/
    QuizController.java          GET /today-plan, POST /, POST /{id}/answers/{no}, GET /{id}/summary
    QuizService.java             Firestore(Admin SDK) 로 생성·채점·요약
    QuizQuestionGenerator.java / MockQuizQuestionGenerator.java   고정 예시 3문제
  global/
    ApiException.java / GlobalExceptionHandler.java
src/main/resources/
  static/login.html   로그인/회원가입 (Firebase Auth compat CDN)
  static/quiz.html    퀴즈봇 (Firebase SDK 안 씀, /api/* 만 호출)
  static/app.css      공용 스타일 (목업 팔레트)
  static/study_plan.json   퀴즈 "오늘의 일과" 예시 데이터 (서버가 읽음)
  static/index.html   옛날 7화면 목업 — 참고용 (동작 안 함)
```

## 실행

### 1. 서비스 계정 키 넣기 (리더가 발급 → 전달)

Firebase 콘솔 → 프로젝트 설정(⚙️) → **서비스 계정** → **새 비공개 키 생성** → 받은 JSON 을
`src/main/resources/` 에 둡니다. 파일명은 둘 중 아무거나:

- `firebase-service-account.json` 으로 이름 바꿔서, 또는
- 콘솔에서 받은 원래 이름 그대로 (예: `planit-ccfff-firebase-adminsdk-xxxx.json`)

`FirebaseConfig` 가 두 경우 다 자동으로 찾습니다. 절대경로로 지정하려면 환경변수
`FIREBASE_CREDENTIALS=/path/to/key.json` 도 됩니다.

이 키는 진짜 비밀키입니다. **커밋 금지** (`.gitignore` 에 `*-firebase-adminsdk-*.json` 등록됨),
노션/드라이브로만 전달합니다. `login.html` 안의 `firebaseConfig`(apiKey 등)와는 완전히 다른
값입니다 — 그건 공개돼도 됩니다.

### 2. 서버 실행

```bash
./gradlew bootRun        # http://localhost:8080
```

`http://localhost:8080/login.html` 에서 로그인 → 자동으로 `quiz.html` 로 이동합니다.

키 파일이 없으면 서버는 뜨지만 `/api/auth/firebase-login` 이 503 을 돌려줍니다.

## Firestore 구조

```
quizzes/{quizId}                       { uid, subjectName, todayScope, quizDate,
                                         createdAt, questions[] }
quizzes/{quizId}/answers/{questionNo}  { selectedChoice, correct, answeredAt }
```

`questions[]` 는 정답 번호·풀이까지 저장하고, 응시용 응답(`POST /api/quizzes`)에서는 빼고 내려줍니다.

## Firebase 콘솔 설정

1. Authentication → 로그인 방법 → **이메일/비밀번호**, **Google** 둘 다 사용 설정
   (Google 은 지원 이메일 지정, 승인된 도메인에 `localhost` 확인)
2. Firestore Database 생성
3. Firestore 보안 규칙: 서버는 Admin SDK 로 규칙을 우회하지만, 클라이언트 직접 접근을
   막기 위해 규칙은 계속 켜 둡니다 (기본 잠금 상태 그대로 두면 됨).
4. 서비스 계정 키 발급 (위 실행 1번)

## 아직 안 한 것

- 퀴즈 문제가 고정 3개입니다. OpenAI 연동은 박지민 담당이고, 정해지면
  `MockQuizQuestionGenerator` 대신 새 `QuizQuestionGenerator` 구현체를 `@Primary` 로 등록하면 됩니다.
- 퀴즈 결과를 체크리스트/계획 재조정과 연결하는 부분은 아직입니다.
