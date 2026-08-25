package com.planit.global.exception;

import lombok.Getter;

/**
 * 요구사항정의서에 정의된 "정상적으로 예상 가능한" 실패 상황(중복 이메일, 형식 오류 등)에 사용하는 예외.
 * 서버 버그가 아니라 업무 규칙 위반이므로 GlobalExceptionHandler 가 4xx 로 변환해서 응답한다.
 */
@Getter
public class BusinessException extends RuntimeException {

	private final ErrorCode errorCode;

	public BusinessException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
