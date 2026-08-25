-- =====================================================================
-- Planit DB 스키마 - 김동호 담당 (회원가입/로그인, 학습계획입력, 퀴즈봇)
--
-- 04_ERD_테이블정의서_김동호담당(회원가입_로그인,학습계획입력,퀴즈봇).xlsx 의
-- "테이블정의서" 시트를 그대로 SQL로 옮긴 것입니다. 컬럼/제약조건을 바꿀 일이 있으면
-- 반드시 그 엑셀 파일도 함께 갱신해 주세요 (두 문서가 어긋나면 안 됩니다).
--
-- 04_ERD_...xlsx 의 "제출 규칙"에 따라 이 파일은 docs/schema.sql 로 커밋합니다.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS planit DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE planit;

-- ---------------------------------------------------------------------
-- 1. member (회원)
-- ---------------------------------------------------------------------
CREATE TABLE member (
    id                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '기본키',
    email               VARCHAR(100) NOT NULL COMMENT '로그인 ID로 씀. 같은 이메일로 중복 가입 안 됨(REQ-A-005)',
    password            VARCHAR(255) NOT NULL COMMENT '암호화(BCrypt)해서 저장. 원문 그대로 저장 금지(REQ-NF-009)',
    name                VARCHAR(30)  NOT NULL COMMENT '회원가입 입력값(REQ-A-001)',
    is_email_verified   BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '인증 끝나기 전에는 로그인 막음(REQ-A-006, REQ-A-015)',
    failed_login_count  INT          NOT NULL DEFAULT 0 COMMENT '5번 연속 틀리면 1분간 로그인 막음(REQ-NF-013)',
    locked_until        DATETIME     NULL DEFAULT NULL COMMENT '이 시각이 지나기 전엔 로그인 막힘(REQ-NF-013)',
    is_deleted          BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '회원 탈퇴 시 실제로 안 지우고 이 값만 켬(공통 규칙)',
    created_at          DATETIME     NOT NULL COMMENT '가입일시',
    updated_at          DATETIME     NULL COMMENT '수정일시',
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='회원 (김동호 담당)';

-- ---------------------------------------------------------------------
-- 2. email_verification (이메일인증)
-- ---------------------------------------------------------------------
CREATE TABLE email_verification (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '기본키',
    member_id    BIGINT       NOT NULL COMMENT 'member 테이블 id 참조',
    token        VARCHAR(255) NOT NULL COMMENT '인증 링크 또는 인증 코드 값',
    expires_at   DATETIME     NOT NULL COMMENT '발급 후 30분까지만 유효(REQ-NF-012)',
    verified_at  DATETIME     NULL DEFAULT NULL COMMENT '비어있으면 아직 인증 안 한 것(REQ-A-006)',
    created_at   DATETIME     NOT NULL COMMENT '발급일시',
    PRIMARY KEY (id),
    UNIQUE KEY uk_email_verification_token (token),
    KEY idx_email_verification_member_id (member_id),
    CONSTRAINT fk_email_verification_member
        FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='이메일 인증 (김동호 담당)';

-- ---------------------------------------------------------------------
-- 3. study_plan (학습계획)
-- ---------------------------------------------------------------------
CREATE TABLE study_plan (
    id                        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '기본키',
    member_id                 BIGINT       NOT NULL COMMENT 'member 테이블 id 참조',
    subject_name              VARCHAR(100) NOT NULL COMMENT '플랜 생성 마법사 1단계 입력값(REQ-B-001)',
    toc_file_url              VARCHAR(500) NULL DEFAULT NULL COMMENT 'PDF, JPG, PNG 파일. 최대 10MB(REQ-B-002, REQ-NF-017)',
    start_date                DATE         NOT NULL COMMENT '플랜 생성 마법사 2단계 입력값(REQ-B-004)',
    end_date                  DATE         NOT NULL COMMENT '시작일보다 빠르면 안 됨(REQ-B-005)',
    daily_available_minutes   INT          NULL DEFAULT NULL COMMENT '확인 필요: 화면에 입력칸이 없어 분 단위로 가정함(REQ-B-007)',
    created_at                DATETIME     NOT NULL COMMENT '등록일시',
    updated_at                DATETIME     NULL COMMENT '수정일시',
    PRIMARY KEY (id),
    KEY idx_study_plan_member_id (member_id),
    KEY idx_study_plan_end_date (end_date),
    CONSTRAINT fk_study_plan_member
        FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='학습계획 (김동호 담당)';

-- ---------------------------------------------------------------------
-- 4. study_plan_time_slot (선호학습시간대)
-- ---------------------------------------------------------------------
CREATE TABLE study_plan_time_slot (
    id             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '기본키',
    study_plan_id  BIGINT      NOT NULL COMMENT 'study_plan 테이블 id 참조',
    time_slot      VARCHAR(20) NOT NULL COMMENT '아침/오전/오후/저녁/심야/주말오전/주말오후/평일만 중 하나(REQ-B-006)',
    created_at     DATETIME    NOT NULL COMMENT '등록일시',
    PRIMARY KEY (id),
    KEY idx_study_plan_time_slot_study_plan_id (study_plan_id),
    CONSTRAINT fk_study_plan_time_slot_study_plan
        FOREIGN KEY (study_plan_id) REFERENCES study_plan (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='선호학습시간대 (김동호 담당)';

-- ---------------------------------------------------------------------
-- 5. quiz (퀴즈)
-- ---------------------------------------------------------------------
CREATE TABLE quiz (
    id             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '기본키',
    member_id      BIGINT   NOT NULL COMMENT 'member 테이블 id 참조',
    study_plan_id  BIGINT   NOT NULL COMMENT 'study_plan 테이블 id 참조(어느 과목에서 낸 퀴즈인지)',
    quiz_date      DATE     NOT NULL COMMENT '체크리스트 100% 완료한 날짜(REQ-Q-001)',
    created_at     DATETIME NOT NULL COMMENT '생성일시. 회원+학습계획+날짜 조합으로 하루 1세트만 생성',
    PRIMARY KEY (id),
    UNIQUE KEY uk_quiz_member_plan_date (member_id, study_plan_id, quiz_date),
    KEY idx_quiz_study_plan_id (study_plan_id),
    CONSTRAINT fk_quiz_member
        FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_quiz_study_plan
        FOREIGN KEY (study_plan_id) REFERENCES study_plan (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='퀴즈 (김동호 담당)';

-- ---------------------------------------------------------------------
-- 6. quiz_question (퀴즈문제)
-- ---------------------------------------------------------------------
CREATE TABLE quiz_question (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '기본키',
    quiz_id        BIGINT       NOT NULL COMMENT 'quiz 테이블 id 참조',
    question_no    INT          NOT NULL COMMENT '1~3번(REQ-Q-002)',
    question_type  VARCHAR(10)  NOT NULL COMMENT 'BASIC(쉬운 문제) 또는 APPLIED(응용 문제)',
    question_text  TEXT         NOT NULL COMMENT '오늘 완료한 학습 범위 안에서만 출제(REQ-Q-003)',
    choice1        VARCHAR(255) NOT NULL COMMENT '4지선다 보기(REQ-Q-002)',
    choice2        VARCHAR(255) NOT NULL,
    choice3        VARCHAR(255) NOT NULL,
    choice4        VARCHAR(255) NOT NULL,
    answer_no      INT          NOT NULL COMMENT '1~4 중 정답 보기 번호',
    explanation    TEXT         NOT NULL COMMENT '제출한 다음 문제 아래에 보여줄 풀이(REQ-Q-005)',
    created_at     DATETIME     NOT NULL COMMENT '생성일시',
    PRIMARY KEY (id),
    KEY idx_quiz_question_quiz_id (quiz_id),
    CONSTRAINT fk_quiz_question_quiz
        FOREIGN KEY (quiz_id) REFERENCES quiz (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='퀴즈문제 (김동호 담당)';

-- ---------------------------------------------------------------------
-- 7. quiz_answer (퀴즈응시답안)
-- ---------------------------------------------------------------------
CREATE TABLE quiz_answer (
    id                 BIGINT   NOT NULL AUTO_INCREMENT COMMENT '기본키',
    quiz_question_id   BIGINT   NOT NULL COMMENT 'quiz_question 테이블 id 참조',
    member_id          BIGINT   NOT NULL COMMENT 'member 테이블 id 참조',
    selected_choice    INT      NOT NULL COMMENT '1~4 중 고른 보기(REQ-Q-004)',
    is_correct         BOOLEAN  NOT NULL COMMENT '제출한 순간 채점해서 저장(REQ-Q-005)',
    answered_at        DATETIME NOT NULL COMMENT '문제 하나당 1번만 제출되게 함(REQ-NF-023 관련)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_quiz_answer_question_member (quiz_question_id, member_id),
    KEY idx_quiz_answer_member_id (member_id),
    CONSTRAINT fk_quiz_answer_question
        FOREIGN KEY (quiz_question_id) REFERENCES quiz_question (id),
    CONSTRAINT fk_quiz_answer_member
        FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='퀴즈응시답안 (김동호 담당)';
