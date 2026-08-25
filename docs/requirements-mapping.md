# 요구사항 ID ↔ 코드 매핑 (김동호 담당)

`02_요구사항정의서.xlsx` 의 요구사항 ID가 실제 코드 어디에 구현되어 있는지 찾기 쉽도록 정리한 표입니다.
화면 ID는 `planit화면흐름도_수정.pptx` 기준입니다.

## 회원가입 / 로그인 (REQ-A-001 ~ REQ-A-015)

| 요구사항 ID | 내용 | 화면 ID | 구현 위치 |
|---|---|---|---|
| REQ-A-001 | 회원가입 입력 (이름/이메일/비번/비번확인) | US-002 | `member.dto.SignUpRequest`, `member.controller.AuthController#signUp` |
| REQ-A-002 | 비밀번호 확인 일치 검증 | US-002 | `member.service.AuthService#signUp` |
| REQ-A-003 | 이메일 형식 검증 | US-002 | `member.service.AuthService#signUp` (EMAIL_PATTERN) |
| REQ-A-004 | 비밀번호 길이 검증 (8자 이상) | US-002 | `member.service.AuthService#signUp` |
| REQ-A-005 | 이메일 중복 검사 | US-002 | `member.service.AuthService#signUp`, DB `member.email` UNIQUE |
| REQ-A-006 | 이메일 인증 발송/제한 | US-003 (신규) | `member.service.EmailVerificationService` |
| REQ-A-007 | 회원가입 완료 후 로그인 화면 이동 | US-002 → US-001 | 프론트 담당 (백엔드는 가입 성공 응답만 내려줌) |
| REQ-A-008 | 로그인 | US-001 | `member.controller.AuthController#login` |
| REQ-A-009 | 로그인 실패 처리 | US-001 | `member.service.AuthService#login` |
| REQ-A-010 | 로그인 성공 시 이동 | US-001 → CM-001 | 프론트 담당 |
| REQ-A-011 | 세션 관리(로그인 상태 유지) | 공통 | `member.service.AuthService#persistLoginSession`, `global.SecurityConfig` |
| REQ-A-012 | 로그아웃 | CM-001(사이드바) | `member.service.AuthService#logout` |
| REQ-A-013 | 로그인/회원가입 화면 전환 링크 | US-001, US-002 | 프론트 담당 |
| REQ-A-014 | Google 소셜 로그인 (C, 이번 범위 제외) | US-001 | 미구현 - 팀 논의 후 우선순위 확정되면 추가 |
| REQ-A-015 | 미인증 계정 로그인 차단 | US-003 | `member.service.AuthService#login`, `member.service.EmailVerificationService#resendVerification` |

## 학습계획입력 (REQ-B-001 ~ REQ-B-011)

| 요구사항 ID | 내용 | 화면 ID | 구현 위치 |
|---|---|---|---|
| REQ-B-001 | 과목/자격증명 입력 | PL-001 STEP1 | `studyplan.dto.StudyPlanCreateRequest` |
| REQ-B-002 | 목차 업로드 (PDF/JPG/PNG) | PL-001 STEP1 | `studyplan.service.LocalFileStorageService` |
| REQ-B-003 | STEP1 필수 입력 검증 | PL-001 STEP1 | `studyplan.service.StudyPlanService#create` |
| REQ-B-004 | 학습 기간 입력 | PL-001 STEP2 | `studyplan.entity.StudyPlan` (startDate/endDate) |
| REQ-B-005 | 학습 기간 유효성 검증 | PL-001 STEP2 | `studyplan.service.StudyPlanService#create` |
| REQ-B-006 | 선호 학습 시간대 선택(복수) | PL-001 STEP2 | `studyplan.entity.TimeSlotType`, `StudyPlanTimeSlot` |
| REQ-B-007 | 하루 가용 학습 시간 입력 | PL-001 STEP2 (추가 설계 필요) | `studyplan.entity.StudyPlan#dailyAvailableMinutes` |
| REQ-B-008 | 과목/단원 우선순위 입력 | PL-001 (추가 설계 필요) | **미구현** - ERD "확인 필요 항목 #1" 참고, 팀 결정 후 추가 |
| REQ-B-009 | 입력 데이터 저장 | PL-001 STEP1~2 | `studyplan.service.StudyPlanService#create` |
| REQ-B-010 | 마법사 단계 이동 | PL-001 | 프론트 담당 (백엔드는 최종 저장 API 하나만 제공) |
| REQ-B-011 | STEP3 진행 화면 표시 | PL-001 STEP3 | 프론트 담당 |

## 퀴즈봇 (REQ-Q-001 ~ REQ-Q-006)

| 요구사항 ID | 내용 | 화면 ID | 구현 위치 |
|---|---|---|---|
| REQ-Q-001 | 퀴즈 응시 트리거 | QZ-001 | `quiz.controller.QuizController#start` |
| REQ-Q-002 | 퀴즈 문제 구성 (기본2 + 응용1) | QZ-001 | `quiz.service.QuizQuestionGenerator`, `MockQuizQuestionGenerator` |
| REQ-Q-003 | 출제 범위 제한 | QZ-001 | `quiz.service.QuizService#start` (todayScope 전달) |
| REQ-Q-004 | 정답 선택 및 제출 | QZ-001 | `quiz.controller.QuizController#submit` |
| REQ-Q-005 | 정답 확인 및 풀이 표시 | QZ-001 | `quiz.dto.QuizSubmitResponse` |
| REQ-Q-006 | 퀴즈 결과 요약 | QZ-001 | `quiz.controller.QuizController#getResultSummary` |

## 비기능 요구사항 (REQ-NF-009 ~ REQ-NF-023, 김동호 담당분)

| 요구사항 ID | 내용 | 구현 위치 |
|---|---|---|
| REQ-NF-009 | 비밀번호 BCrypt 암호화 | `global.SecurityConfig#passwordEncoder` |
| REQ-NF-010 | 이메일 유일성 제약 | `docs/schema.sql` (`uk_member_email`) |
| REQ-NF-011 | 세션 안전 저장 (HttpOnly 등) | `global.SecurityConfig`, `application.yml` |
| REQ-NF-012 | 인증 메일 30분 유효 | `member.service.EmailVerificationService` |
| REQ-NF-013 | 로그인 5회 실패 시 1분 차단 | `member.entity.Member#increaseFailedLoginCount` |
| REQ-NF-014 | 로그인/회원가입 2초 이내 응답 | 성능 목표 - 별도 코드 없음 (부하 테스트로 확인 필요) |
| REQ-NF-015 | 입력 오류 안내 위치 | 프론트 담당 (백엔드는 `ErrorResponse.fieldErrors` 로 필드명 제공) |
| REQ-NF-016 | 예외 로그 | `global.exception.GlobalExceptionHandler` |
| REQ-NF-017 | 업로드 파일 10MB 제한 | `application.yml` (`spring.servlet.multipart`) |
| REQ-NF-018 | 업로드 응답 시간/진행률 | 프론트 담당 (진행률 UI) |
| REQ-NF-019 | 본인 학습 계획만 접근 | `studyplan.service.StudyPlanService#findAndValidateOwner` |
| REQ-NF-020 | 학습 계획 데이터 보존 | 논리 삭제 정책 (물리 삭제 API 없음) |
| REQ-NF-021 | 마법사 화면 모바일 대응 | 프론트 담당 |
| REQ-NF-022 | 브라우저 지원 | 프론트 담당 |
| REQ-NF-023 | 퀴즈 응시 기록 보존 | `quiz.entity.QuizAnswer` (삭제 API 없음, 기록 영구 보관) |

## 아직 팀 논의가 필요한 항목

`04_ERD_테이블정의서...xlsx` 의 "확인 필요 항목" 시트와 동일합니다.

1. **REQ-B-008 과목/단원 우선순위**: 한 학습계획에 과목을 여러 개 등록할 수 있는지부터 정해야 구현 가능. 이번 버전에는 미구현.
2. **퀴즈 문제 생성 방식**: AI가 그때그때 생성 vs 미리 만든 문제 은행. 지금은 `QuizQuestionGenerator` 인터페이스만 만들고, 기본 구현체(`MockQuizQuestionGenerator`)는 고정 예시 문제를 반환한다. OpenAI 연동 방식이 정해지면(박지민 담당) `OpenAiQuizQuestionGenerator` 를 새로 만들어 교체하면 된다.
3. **퀴즈 결과와 체크리스트/계획 재조정 연동 여부**: 유시우(학습 계획 조회), 김경태·박지민(계획 재조정) 담당 기능과 관련되어 이번 버전에는 연결하지 않았다.
