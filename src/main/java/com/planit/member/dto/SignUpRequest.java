package com.planit.member.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 회원가입 입력 (REQ-A-001).
 * 이름/이메일/비밀번호/비밀번호 확인 중 하나라도 비어 있으면 @NotBlank 에서 먼저 걸린다.
 * 이메일 형식(REQ-A-003), 비밀번호 길이(REQ-A-004), 비밀번호 확인 일치(REQ-A-002),
 * 이메일 중복(REQ-A-005) 은 AuthService 에서 순서대로 검증한다.
 */
public record SignUpRequest(

	@NotBlank(message = "이름을 입력해 주세요")
	String name,

	@NotBlank(message = "이메일을 입력해 주세요")
	String email,

	@NotBlank(message = "비밀번호를 입력해 주세요")
	String password,

	@NotBlank(message = "비밀번호 확인을 입력해 주세요")
	String passwordConfirm
) {
}
