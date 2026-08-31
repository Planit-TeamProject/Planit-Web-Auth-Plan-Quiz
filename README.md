# Planit - 김동호 담당 파트

Planit(학습 계획 관리 서비스) 팀 프로젝트에서 **김동호가 담당한 3개 기능**의 구현입니다.

- 회원가입 / 로그인 (구글 로그인 포함)
- 학습 계획 입력 (플랜 생성 마법사)
- 퀴즈봇

담당 근거 문서 (팀 저장소 `노예/` 폴더):
- `01_프로젝트_기획서.docx`
- `02_요구사항정의서.xlsx` → REQ-A(인증), REQ-B(학습계획입력), REQ-Q(퀴즈봇), REQ-NF-009~023
- `04_ERD_테이블정의서_김동호담당(회원가입_로그인,학습계획입력,퀴즈봇).xlsx`
- `planit화면흐름도_수정.pptx` (US-001/US-002/US-003, PL-001, QZ-001)

**요구사항 ID ↔ 코드 위치는 [`docs/requirements-mapping.md`](docs/requirements-mapping.md) 참고.**

## 아키텍처

기존 **Spring Boot + MySQL** 백엔드를 **Firebase** 로 전환했습니다.
안드로이드 앱과 같은 Firebase 프로젝트를 써서 앱/웹 사용자가 같은 Authentication·Firestore 를 공유합니다.

| 구분 | 선택 |
|---|---|
| 프론트엔드 | React 18 + TypeScript, Vite |
| 인증 | Firebase Authentication (이메일/비밀번호 + Google) |
| 데이터 | Cloud Firestore (`users`, `studyPlans`, `quizzes`) |
| 파일 | Firebase Storage (목차 파일) |
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
    ├── src/firebase.ts           # Firebase 초기화 (auth / db / storage)
    ├── src/auth/useCurrentUser.ts
    ├── src/api/
    │   ├── auth.ts               # 회원가입/로그인/구글 로그인
    │   ├── quiz.ts               # 퀴즈 생성·제출·요약 (Firestore)
    │   ├── quizQuestions.ts      # 문제 생성기 (고정 예시, OpenAI 연동 시 교체)
    │   └── studyPlan.ts          # 학습계획 CRUD + 목차 업로드
    └── src/pages/
        ├── LoginPage.tsx  SignupPage.tsx   # 화면 2 / 3
        ├── StudyPlanPage.tsx               # 화면 4 (플랜 생성 마법사)
        └── QuizPage.tsx                    # 화면 6 (오늘의 퀴즈)
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
사용 설정, Firestore·Storage 보안 규칙)이 정리되어 있습니다.

### Spring (정적 서빙만, 선택)

```bash
./gradlew bootRun              # http://localhost:8080  (목업 index.html)
```

DB 설정이 필요 없습니다.

## Firestore 데이터 모델

```
users/{uid}                            { name, email, createdAt }
studyPlans/{planId}                    { uid, subjectName, tocFileUrl, startDate, endDate,
                                         dailyAvailableMinutes, timeSlots[], createdAt, updatedAt }
quizzes/{quizId}                       { uid, studyPlanId, subjectName, todayScope, quizDate,
                                         createdAt, questions[] }
quizzes/{quizId}/answers/{questionNo}  { selectedChoice, correct, answeredAt }
```

## 이번 버전에서 일부러 빼놓은 것 (팀 논의 필요)

1. **REQ-B-008 과목/단원 우선순위**: 한 학습계획에 과목을 여러 개 등록할 수 있는지부터 팀 결정 필요. 미구현.
2. **퀴즈 문제 생성 방식**: 지금은 `frontend/src/api/quizQuestions.ts` 가 고정 예시 3문제를 반환.
   OpenAI 연동(박지민 담당)이 정해지면 `generateQuestions` 만 교체하면 됩니다.
3. **퀴즈 결과 ↔ 체크리스트/계획 재조정 연동**: 유시우·김경태·박지민 담당 기능과 연결 필요. 이번 버전 미연결.
