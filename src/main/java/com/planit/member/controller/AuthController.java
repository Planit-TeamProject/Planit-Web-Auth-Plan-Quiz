package com.planit.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.planit.member.dto.LoginRequest;
import com.planit.member.dto.MemberResponse;
import com.planit.member.dto.SignUpRequest;
import com.planit.member.service.AuthService;
import com.planit.member.service.EmailVerificationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 회원가입 / 로그인 / 로그아웃 / 이메일 인증 API.
 *
 * 관련 화면 (planit화면흐름도_수정.pptx 기준):
 *  - POST /api/auth/signup              -> US-002 (회원가입)
 *  - POST /api/auth/login               -> US-001 (로그인)
 *  - POST /api/auth/logout              -> CM-001(사이드바)
 *  - GET  /api/auth/email-verification  -> US-003 (이메일 인증, 신규 추가한 화면)
 */
@RestController
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final EmailVerificationService emailVerificationService;

	/** REQ-A-001 ~ REQ-A-006 */
	@PostMapping("/api/auth/signup")
	public ResponseEntity<MemberResponse> signUp(@Valid @RequestBody SignUpRequest request) {
		MemberResponse response = authService.signUp(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/** REQ-A-008 ~ REQ-A-011, REQ-A-015 */
	@PostMapping("/api/auth/login")
	public ResponseEntity<MemberResponse> login(
		@Valid @RequestBody LoginRequest request,
		HttpServletRequest servletRequest
	) {
		return ResponseEntity.ok(authService.login(request, servletRequest));
	}

	/** REQ-A-012 */
	@PostMapping("/api/auth/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		authService.logout(request, response);
		return ResponseEntity.noContent().build();
	}

	/** REQ-A-006: 이메일 인증 링크(코드) 클릭 시 호출 */
	@GetMapping("/api/auth/email-verification")
	public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
		emailVerificationService.verify(token);
		return ResponseEntity.ok().build();
	}

	/**
	 * REQ-A-015: 인증 메일 재발송.
	 * 이메일 인증 전에는 로그인이 막혀 세션이 없으므로, 로그인 실패 화면에서 입력했던 이메일을 그대로 넘겨받는다.
	 */
	@PostMapping("/api/auth/email-verification/resend")
	public ResponseEntity<Void> resendVerification(@RequestParam String email) {
		emailVerificationService.resendVerification(email);
		return ResponseEntity.ok().build();
	}
}
