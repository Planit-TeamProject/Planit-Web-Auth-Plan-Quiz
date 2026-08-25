package com.planit.global.exception;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * REQ-NF-015: 모든 입력 오류는 해당 입력 필드 바로 아래에 문장으로 표시한다.
 * -> 프론트에서 필드별로 매칭할 수 있도록 field 를 함께 내려준다. (필드 특정이 안 되는 오류는 field 가 null)
 */
@Getter
@Builder
public class ErrorResponse {

	private final String code;      // ErrorCode enum name (예: "PASSWORD_CONFIRM_MISMATCH")
	private final String message;   // 화면에 그대로 보여줄 한국어 문구
	private final List<FieldError> fieldErrors;

	@Getter
	@Builder
	public static class FieldError {
		private final String field;
		private final String reason;
	}
}
