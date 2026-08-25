package com.planit.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 서비스 전역 에러 코드.
 * message 는 02_요구사항정의서.xlsx 에 명시된 문구를 그대로 사용한다 (화면에 그대로 노출되는 문구이므로 임의로 바꾸지 말 것).
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// ===== 회원가입 / 로그인 (REQ-A-xxx) =====
	// REQ-A-001 (필수 입력)은 SignUpRequest 의 @NotBlank 에서 걸러져 INVALID_INPUT_VALUE 로 응답한다.
	PASSWORD_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다"),
	INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "이메일 형식이 올바르지 않습니다"),
	PASSWORD_TOO_SHORT(HttpStatus.BAD_REQUEST, "비밀번호는 8자 이상이어야 합니다"),
	EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 가입된 이메일입니다"),
	LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다"),
	LOGIN_LOCKED(HttpStatus.LOCKED, "로그인 실패 횟수를 초과했습니다. 1분 후 다시 시도해 주세요."),
	EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "이메일 인증 후 로그인할 수 있습니다"),
	VERIFICATION_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "인증 링크가 올바르지 않습니다"),
	VERIFICATION_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "인증 유효 시간이 만료되었습니다. 인증 메일을 다시 받아주세요"),
	LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다"),

	// ===== 학습계획입력 (REQ-B-xxx) =====
	// REQ-B-003 원문: "과목명과 목차 파일 중 하나라도 입력되지 않으면 오류" -> 즉 둘 다 있어야 통과
	STUDY_PLAN_STEP1_REQUIRED(HttpStatus.BAD_REQUEST, "과목명과 목차 파일을 모두 입력해 주세요"),
	STUDY_PLAN_PERIOD_INVALID(HttpStatus.BAD_REQUEST, "종료일(시험일)은 시작일보다 빠를 수 없습니다"),
	STUDY_PLAN_TOC_FILE_TYPE_INVALID(HttpStatus.BAD_REQUEST, "목차 파일은 PDF, JPG, PNG 파일만 업로드할 수 있습니다"),
	STUDY_PLAN_TOC_FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "목차 파일은 최대 10MB까지 업로드할 수 있습니다"),
	STUDY_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "학습 계획을 찾을 수 없습니다"),
	STUDY_PLAN_ACCESS_DENIED(HttpStatus.FORBIDDEN, "본인이 등록한 학습 계획만 조회/수정할 수 있습니다"),

	// ===== 퀴즈봇 (REQ-Q-xxx) =====
	QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "퀴즈를 찾을 수 없습니다"),
	QUIZ_ACCESS_DENIED(HttpStatus.FORBIDDEN, "본인의 퀴즈만 조회/응시할 수 있습니다"),
	QUIZ_ALREADY_EXISTS_FOR_TODAY(HttpStatus.CONFLICT, "오늘 응시할 수 있는 퀴즈가 이미 생성되어 있습니다"),
	QUIZ_QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "퀴즈 문제를 찾을 수 없습니다"),
	QUIZ_ALREADY_ANSWERED(HttpStatus.CONFLICT, "이미 제출한 문제입니다"),
	QUIZ_CHOICE_OUT_OF_RANGE(HttpStatus.BAD_REQUEST, "보기 번호는 1~4 중 하나여야 합니다"),

	// ===== 공통 =====
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다"),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다");

	private final HttpStatus status;
	private final String message;
}
