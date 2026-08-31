# Planit - 김동호 담당 파트

Planit(학습 계획 관리 서비스) 팀 프로젝트에서 **김동호가 담당한 기능**의 구현입니다.

- 회원가입 / 로그인 (구글 로그인 포함) / 로그아웃
- 퀴즈봇

> 학습계획입력(플랜 생성 마법사)은 다른 담당이라 이 저장소에 포함하지 않습니다.

담당 근거 문서 (팀 저장소 `노예/` 폴더):
- `01_프로젝트_기획서.docx`
- `02_요구사항정의서.xlsx` → REQ-A(인증), REQ-Q(퀴즈봇), REQ-NF-009~023
- `planit화면흐름도_수정.pptx` (US-001/US-002/US-003, QZ-001)

**요구사항 ID ↔ 코드 위치는 [`docs/requirements-mapping.md`](docs/requirements-mapping.md) 참고.**

## 아키텍처

기존 **Spring Boot + MySQL** 백엔드를 **Firebase** 로 전환했습니다.
안드로이드 앱과 같은 Firebase 프로젝트를 써서 앱/웹 사용자가 같은 Authentication·Firestore 를 공유합니다.

| 구분 | 선택 |
|---|---|
| 프론트엔드 | React 18 + TypeScript, Vite |
| 인증 | Firebase Authentication (이메일/비밀번호 + Google) |
| 데이터 | Cloud Firestore (`users`, `quizzes`) |
| 서버 | Spring Boot 3.3.x — 정적 리소스 서빙만 (DB 없음) |

MySQL / JPA / Spring Security / Spring Data JPA 의존성은 모두 제거되었습니다.
데이터·인증 연동 코드는 전부 `frontend/` 에 있습니다.

## 폴더 구조

```
팀프로젝트/
├── build.gradle                  # web + lombok 만 남음
├── src/main/java/com/planit/
│   └── PlanitApplication.java    # 정적 리소스 서빙 전용 (MySQL/JPA 제거됨)
├── src/main/resources/static/    # 초기 화면 목업(index.html) — 참고용
├── docs/requirements-mapping.md  # 요구사항 ID → 코드 위치
└── frontend/                     # ★ 실제 기능 구현 (React + Firebase)
    ├── src/firebase.ts           # Firebase 초기화 (auth / db)
    ├── src/auth/useCurrentUser.ts
    ├── src/api/
    │   ├── auth.ts               # 회원가입/로그인/구글 로그인/로그아웃
    │   ├── quiz.ts               # 퀴즈 생성·제출·요약 (Firestore)
    │   └── quizQuestions.ts      # 문제 생성기 (고정 예시, OpenAI 연동 시 교체)
    ├── src/pages/
    │   ├── LoginPage.tsx  SignupPage.tsx   # 화면 2 / 3
    │   └── QuizPage.tsx                    # 화면 6 (오늘의 퀴즈)
    └── src/styles/app.css        # 목업에서 추린 디자인 토큰·버튼·카드 스타일
```

## 실행

### 프론트엔드 (실제 기능)

```bash
cd frontend
npm install
cp .env.example .env.local     # Firebase 웹 앱 config 값 채우기
npm run dev                    # http://localhost:3000
```

`frontend/README.md` 에 Firebase 콘솔에서 해야 하는 설정(웹 앱 등록, 이메일/Google 공급업체
사용 설정, Firestore 보안 규칙)이 정리되어 있습니다.

### Spring (정적 서빙만, 선택)

```bash
./gradlew bootRun              # http://localhost:8080  (목업 index.html)
```

DB 설정이 필요 없습니다.

## Firestore 데이터 모델

```
users/{uid}                            { name, email, createdAt }
quizzes/{quizId}                       { uid, subjectName, todayScope, quizDate,
                                         createdAt, questions[] }
quizzes/{quizId}/answers/{questionNo}  { selectedChoice, correct, answeredAt }
```

## 이번 버전에서 일부러 빼놓은 것 (팀 논의 필요)

1. **퀴즈 문제 생성 방식**: 지금은 `frontend/src/api/quizQuestions.ts` 가 고정 예시 3문제를 반환.
   OpenAI 연동(박지민 담당)이 정해지면 `generateQuestions` 만 교체하면 됩니다.
2. **퀴즈 결과 ↔ 체크리스트/계획 재조정 연동**: 유시우·김경태·박지민 담당 기능과 연결 필요. 이번 버전 미연결.
