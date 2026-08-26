package com.planit.global.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

/**
 * REQ-NF-016: 서버 예외 발생 시 로그에 예외 종류와 발생 위치를 남긴다.
 * 모든 컨트롤러의 예외를 한 곳에서 잡아 일관된 형태(ErrorResponse)로 응답한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/** 업무 규칙 위반 (이메일 중복, 비밀번호 불일치 등) */
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
		ErrorCode errorCode = e.getErrorCode();
		log.warn("[BusinessException] {} - {}", errorCode.name(), errorCode.getMessage());
		return ResponseEntity.status(errorCode.getStatus())
			.body(ErrorResponse.builder()
				.code(errorCode.name())
				.message(errorCode.getMessage())
				.build());
	}

	/** @Valid 로 걸리는 DTO 검증 실패 (필드별 오류 메시지를 함께 내려줌) */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
		List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
			.map(fe -> ErrorResponse.FieldError.builder()
				.field(fe.getField())
				.reason(fe.getDefaultMessage())
				.build())
			.toList();

		log.warn("[ValidationException] {}", fieldErrors);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ErrorResponse.builder()
				.code(ErrorCode.INVALID_INPUT_VALUE.name())
				.message(ErrorCode.INVALID_INPUT_VALUE.getMessage())
				.fieldErrors(fieldErrors)
				.build());
	}

	/**
	 * 필수 쿼리 파라미터(예: /api/auth/email-verification?token=... 의 token)가 빠진 경우.
	 * 이걸 따로 안 잡으면 아래 handleException 으로 떨어져서 500 으로 잘못 보이게 된다.
	 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException e) {
		log.warn("[MissingServletRequestParameterException] {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ErrorResponse.builder()
				.code(ErrorCode.INVALID_INPUT_VALUE.name())
				.message("%s 파라미터가 필요합니다".formatted(e.getParameterName()))
				.build());
	}

	/** 쿼리 파라미터/경로 변수의 타입이 안 맞는 경우 (예: 숫자여야 하는 id 에 문자열이 들어옴) */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
		log.warn("[MethodArgumentTypeMismatchException] {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ErrorResponse.builder()
				.code(ErrorCode.INVALID_INPUT_VALUE.name())
				.message("%s 값의 형식이 올바르지 않습니다".formatted(e.getName()))
				.build());
	}

	/**
	 * REQ-NF-017: 요청 자체가 application.yml 의 multipart 제한(10MB)을 넘으면
	 * 컨트롤러에 도달하기 전에 이 예외가 발생한다. LocalFileStorageService 의 크기 검사와 같은 메시지로 통일한다.
	 */
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
		ErrorCode errorCode = ErrorCode.STUDY_PLAN_TOC_FILE_TOO_LARGE;
		log.warn("[MaxUploadSizeExceededException] {}", e.getMessage());
		return ResponseEntity.status(errorCode.getStatus())
			.body(ErrorResponse.builder()
				.code(errorCode.name())
				.message(errorCode.getMessage())
				.build());
	}

	/**
	 * 존재하지 않는 정적 리소스 요청 (예: 브라우저가 자동으로 요청하는 /favicon.ico).
	 * 따로 안 잡으면 아래 handleException 으로 떨어져 ERROR 스택트레이스가 매 요청마다 찍힌다.
	 * 실제 장애가 아니므로 404 만 조용히 내려준다.
	 */
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException e) {
		log.debug("[NoResourceFoundException] {}", e.getResourcePath());
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(ErrorResponse.builder()
				.code(HttpStatus.NOT_FOUND.name())
				.message("요청한 리소스를 찾을 수 없습니다")
				.build());
	}

	/** 그 외 예상하지 못한 서버 오류 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		log.error("[UnhandledException] {}", e.getMessage(), e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ErrorResponse.builder()
				.code(ErrorCode.INTERNAL_SERVER_ERROR.name())
				.message(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
				.build());
	}
}
