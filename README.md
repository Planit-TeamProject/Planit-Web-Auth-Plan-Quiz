# Planit 백엔드 - 김동호 담당 파트

Planit(학습 계획 관리 서비스) 팀 프로젝트에서 **김동호가 담당한 3개 기능**의 백엔드 구현입니다.

- 회원가입 / 로그인
- 학습 계획 입력 (플랜 생성 마법사)
- 퀴즈봇

담당 근거 문서 (팀 저장소 `노예/` 폴더):
- `01_프로젝트_기획서.docx` (6. 기술 스택)
- `02_요구사항정의서.xlsx` → REQ-A(인증), REQ-B(학습계획입력), REQ-Q(퀴즈봇), REQ-NF-009~023
- `04_ERD_테이블정의서_김동호담당(회원가입_로그인,학습계획입력,퀴즈봇).xlsx`
- `planit화면흐름도_수정.pptx` (US-001/US-002/US-003, PL-001, QZ-001)

**요구사항 ID ↔ 실제 코드 위치는 [`docs/requirements-mapping.md`](docs/requirements-mapping.md) 에 전부 정리되어 있습니다.**
코드를 처음 보는 팀원은 이 문서부터 보는 걸 추천합니다.

## 기술 스택

기획서 "6. 기술 스택"을 그대로 따릅니다.

| 구분 | 선택 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.x |
| DB 접근 | Spring Data JPA |
| 인증 | Spring Security (세션 기반) |
| DB | MySQL 8 |
| 기타 | Lombok |

## 폴더 구조

```
팀프로젝트/
├── build.gradle
├── docs/
│   ├── schema.sql                # CREATE TABLE 전체 (04_ERD_...xlsx 와 1:1로 맞춰져 있음)
│   └── requirements-mapping.md   # 요구사항 ID -> 코드 위치
└── src/main/java/com/planit/
    ├── global/                   # 전역 설정, 공통 예외 처리
    ├── member/                   # 회원가입 / 로그인 / 이메일 인증
    │   ├── entity/    Member, EmailVerification
    │   ├── repository/
    │   ├── dto/
    │   ├── service/   AuthService, EmailVerificationService, MemberDetails(Service)
    │   └── controller/ AuthController
    ├── studyplan/                 # 학습계획입력 (플랜 생성 마법사)
    │   ├── entity/    StudyPlan, StudyPlanTimeSlot, TimeSlotType
    │   ├── repository/
    │   ├── dto/
    │   ├── service/   StudyPlanService, FileStorageService(로컬 저장 구현체)
    │   └── controller/ StudyPlanController
    └── quiz/                      # 퀴즈봇
        ├── entity/    Quiz, QuizQuestion, QuizAnswer, QuestionType
        ├── repository/
        ├── dto/
        ├── service/   QuizService, QuizQuestionGenerator(현재는 Mock 구현체)
        └── controller/ QuizController
```

패키지는 **기능(도메인) 기준**으로 나눴습니다 (member / studyplan / quiz). 다른 팀원이 맡은 기능(예: 학습 계획
조회·체크, 계획 재조정, 통계 대시보드)을 추가할 때는 같은 방식으로 새 패키지(`com.planit.〇〇`)를 만들면 됩니다.

## 로컬 실행 방법

1. MySQL 에 `docs/schema.sql` 을 실행해 테이블을 만듭니다.
   ```
   mysql -u root -p < docs/schema.sql
   ```
2. DB 접속 정보는 커밋하지 않습니다. 환경변수로 넘기거나(권장), 로컬에 `application-local.yml` 을 만들어 사용하세요.
   ```
   set DB_URL=jdbc:mysql://localhost:3306/planit?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
   set DB_USERNAME=root
   set DB_PASSWORD=your-password
   ```
3. 실행
   ```
   ./gradlew bootRun
   ```
4. 기본 포트는 `8080`, 프론트(React, `localhost:3000`)와의 CORS/세션 쿠키 설정은 `global/SecurityConfig` 에 있습니다.

> 참고: `gradle-wrapper.jar` 등 Gradle Wrapper 파일은 이 커밋에 포함되어 있지 않습니다.
> 처음 받은 사람은 `gradle wrapper` 를 한 번 실행해 wrapper 를 생성한 뒤 커밋해 주세요.
>
> 검증: 이 코드는 Gradle 8.10 + JDK 21로 `compileJava`/`compileTestJava`/`test` 를 실제로 실행해
> 컴파일 성공과 단위 테스트(`MemberTest`, `AuthServiceTest`, `StudyPlanServiceTest`, `QuizQuestionTest`,
> 총 12개) 전부 통과를 확인했습니다. 다만 로컬 폴더 경로에 한글이 섞여 있으면(예: `노예/팀프로젝트`)
> Windows 코드페이지 문제로 `gradle test` 워커가 클래스를 못 찾는 경우가 있었습니다 — 영문 경로로
> 클론해서 실행하거나 CI(GitHub Actions 등 리눅스 환경)에서 돌리면 문제없이 통과합니다.

## API 목록

| Method | URL | 설명 | 요구사항 ID |
|---|---|---|---|
| POST | `/api/auth/signup` | 회원가입 | REQ-A-001~006 |
| POST | `/api/auth/login` | 로그인 | REQ-A-008~011, 015 |
| POST | `/api/auth/logout` | 로그아웃 | REQ-A-012 |
| GET  | `/api/auth/email-verification?token=` | 이메일 인증 확인 | REQ-A-006 |
| POST | `/api/auth/email-verification/resend?email=` | 인증 메일 재발송 | REQ-A-015 |
| POST | `/api/study-plans/toc-file` | 목차 파일 업로드 (multipart) | REQ-B-002 |
| POST | `/api/study-plans` | 학습 계획 생성 | REQ-B-001~009 |
| GET  | `/api/study-plans` | 내 학습 계획 목록 | - |
| GET  | `/api/study-plans/{id}` | 내 학습 계획 상세 | REQ-NF-019 |
| POST | `/api/quizzes` | 오늘의 퀴즈 생성(트리거) | REQ-Q-001~003 |
| GET  | `/api/quizzes/{id}` | 퀴즈 조회 | - |
| POST | `/api/quizzes/questions/{questionId}/answers` | 문제 제출 | REQ-Q-004~005 |
| GET  | `/api/quizzes/{id}/summary` | 결과 요약 | REQ-Q-006 |

로그인/회원가입 관련 API 를 제외한 나머지는 모두 로그인(세션 쿠키)이 필요합니다.

## 이번 버전에서 일부러 빼놓은 것 (팀 논의 필요)

`docs/requirements-mapping.md` 맨 아래 "아직 팀 논의가 필요한 항목" 참고. 요약하면:

1. **과목/단원 우선순위 입력(REQ-B-008)**: 한 학습계획에 과목을 여러 개 등록할 수 있는지부터 팀 결정이 필요해서 미구현.
2. **퀴즈 문제 생성**: 지금은 `QuizQuestionGenerator` 인터페이스 뒤에 고정 예시 문제를 주는 `MockQuizQuestionGenerator` 만 있습니다.
   OpenAI 연동 방식이 정해지면 이 인터페이스를 구현하는 클래스만 새로 추가하면 되고, `QuizService` 코드는 안 바꿔도 됩니다.
3. **Google 소셜 로그인(REQ-A-014)**: 요구사항 우선순위가 C(Could have)라 이번 버전엔 없습니다.
4. **파일 저장소**: 지금은 로컬 디스크(`./uploads/toc`)에 저장합니다. 배포 환경이 정해지면
   `FileStorageService` 를 구현하는 클래스(S3 등)만 새로 추가하면 됩니다.
