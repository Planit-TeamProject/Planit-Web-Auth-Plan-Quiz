# 요구사항 ID ↔ 코드 매핑 (김동호 담당)

`02_요구사항정의서.xlsx`의 요구사항 ID가 코드 어디에 있는지 찾는 표입니다. 화면 ID는
`planit화면흐름도_수정.pptx` 기준입니다.

Spring + MySQL에서 Firebase로 옮겼기 때문에 구현 위치는 전부 `frontend/`(React) 안입니다.
학습계획입력(REQ-B)은 다른 담당이라 여기 없습니다.

## 회원가입 / 로그인 (REQ-A-001 ~ REQ-A-015)

| 요구사항 ID | 내용 | 화면 ID | 구현 위치 |
|---|---|---|---|
| REQ-A-001 | 회원가입 입력 (이름/이메일/비번/비번확인) | US-002 | `pages/SignupPage.tsx` |
| REQ-A-002 | 비밀번호 확인 일치 검증 | US-002 | `pages/SignupPage.tsx#validate` |
| REQ-A-003 | 이메일 형식 검증 | US-002 | Firebase Auth (`auth/invalid-email` → `api/auth.ts#authErrorMessage`) |
| REQ-A-004 | 비밀번호 길이 검증 (8자 이상) | US-002 | `pages/SignupPage.tsx#validate` (Firebase 최소 6자) |
| REQ-A-005 | 이메일 중복 검사 | US-002 | Firebase Auth (`auth/email-already-in-use`) |
| REQ-A-006 | 이메일 인증 | US-003 | Firebase Auth 이메일 인증 기능 사용 (콘솔 설정) |
| REQ-A-007 | 회원가입 완료 후 로그인 화면 이동 | US-002 → US-001 | `pages/SignupPage.tsx` (`navigate('/login')`) |
| REQ-A-008 | 로그인 | US-001 | `pages/LoginPage.tsx`, `api/auth.ts#signIn` |
| REQ-A-009 | 로그인 실패 처리 | US-001 | `api/auth.ts#authErrorMessage` (`auth/invalid-credential` 등) |
| REQ-A-010 | 로그인 성공 시 이동 | US-001 → CM-001 | `pages/LoginPage.tsx` (성공 분기 — 팀원 웹 연결 지점) |
| REQ-A-011 | 세션 관리(로그인 상태 유지) | 공통 | Firebase Auth 세션 지속 + `auth/useCurrentUser.ts` |
| REQ-A-012 | 로그아웃 | CM-001 | `api/auth.ts#signOutUser` |
| REQ-A-013 | 로그인/회원가입 화면 전환 링크 | US-001, US-002 | `pages/LoginPage.tsx`, `pages/SignupPage.tsx` (`<Link>`) |
| REQ-A-014 | Google 소셜 로그인 | US-001 | `api/auth.ts#signInWithGoogle` (팝업 → 리다이렉트 폴백) |
| REQ-A-015 | 미인증 계정 로그인 차단 | US-003 | Firebase Auth (`auth/user-disabled`) |

## 학습계획입력 (REQ-B-001 ~ REQ-B-011)

이 저장소에서는 다루지 않습니다 (다른 담당).

## 퀴즈봇 (REQ-Q-001 ~ REQ-Q-006)

| 요구사항 ID | 내용 | 화면 ID | 구현 위치 |
|---|---|---|---|
| REQ-Q-001 | 퀴즈 응시 트리거 | QZ-001 | `pages/QuizPage.tsx#handleStart`, `api/quiz.ts#startQuiz` |
| REQ-Q-002 | 퀴즈 문제 구성 (기본2 + 응용1) | QZ-001 | `api/quizQuestions.ts#generateQuestions` |
| REQ-Q-003 | 출제 범위 제한 | QZ-001 | `pages/QuizPage.tsx#loadDayOne` (todayScope 전달) |
| REQ-Q-004 | 정답 선택 및 제출 | QZ-001 | `api/quiz.ts#submitAnswer` (Firestore `quizzes/{id}/answers`) |
| REQ-Q-005 | 정답 확인 및 풀이 표시 | QZ-001 | `pages/QuizPage.tsx` (QuestionCard 결과 표시) |
| REQ-Q-006 | 퀴즈 결과 요약 | QZ-001 | `api/quiz.ts#getQuizSummary` |

## 비기능 요구사항 (REQ-NF-009 ~ REQ-NF-023, 김동호 담당분)

| 요구사항 ID | 내용 | 구현 위치 |
|---|---|---|
| REQ-NF-009 | 비밀번호 암호화 저장 | Firebase Authentication 이 관리 (앱에서 비밀번호를 저장/처리하지 않음) |
| REQ-NF-010 | 이메일 유일성 | Firebase Authentication (이메일당 계정 1개) |
| REQ-NF-011 | 세션 안전 저장 | Firebase Auth SDK (IndexedDB, 토큰 자동 갱신) |
| REQ-NF-012 | 인증 메일 유효시간 | Firebase Auth 이메일 인증 설정 (콘솔) |
| REQ-NF-013 | 로그인 반복 실패 차단 | Firebase Auth (`auth/too-many-requests`) |
| REQ-NF-014 | 로그인/회원가입 2초 이내 응답 | 성능 목표 — 별도 코드 없음 |
| REQ-NF-015 | 입력 오류 안내 위치 | `api/auth.ts#authErrorMessage` + 각 페이지 `<p role="alert">` |
| REQ-NF-016 | 예외 로그 | 각 페이지 `catch` → 화면 표시 + `console` |
| REQ-NF-017~019 | 목차 업로드/학습계획 접근 제한 | 학습계획입력 담당(다른 담당) |
| REQ-NF-022 | 브라우저 지원 | 프론트 |
| REQ-NF-023 | 퀴즈 응시 기록 보존 | Firestore `quizzes/{id}/answers` (삭제 API 없음) |

## 아직 팀 논의가 필요한 항목

1. **퀴즈 문제 생성 방식**: 지금은 `api/quizQuestions.ts` 가 고정 예시 3문제 반환.
   OpenAI 연동(박지민 담당)이 정해지면 `generateQuestions` 만 교체.
2. **퀴즈 결과 ↔ 체크리스트/계획 재조정 연동**: 유시우·김경태·박지민 담당 기능과 연결 필요. 미연결.
3. **Firestore 보안 규칙**: `users`·`quizzes` 는 본인(uid) 문서만 읽기/쓰기 가능하도록
   규칙을 콘솔에 작성해야 함 (`frontend/README.md` 참고).
