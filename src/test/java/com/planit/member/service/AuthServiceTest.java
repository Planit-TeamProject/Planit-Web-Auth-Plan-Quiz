package com.planit.member.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.planit.global.exception.BusinessException;
import com.planit.global.exception.ErrorCode;
import com.planit.member.dto.SignUpRequest;
import com.planit.member.repository.MemberRepository;

/**
 * 회원가입 검증 순서(REQ-A-002 ~ REQ-A-005)에 대한 단위 테스트.
 * (로그인은 AuthenticationManager 를 거치는 통합 테스트 영역이라 여기서는 다루지 않는다)
 * 실제 DB/Security 없이 Mockito 로 의존성만 흉내 낸다.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private EmailVerificationService emailVerificationService;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private LoginFailureRecorder loginFailureRecorder;

	@InjectMocks
	private AuthService authService;

	@Test
	void 비밀번호와_비밀번호확인이_다르면_예외가_발생한다() {
		SignUpRequest request = new SignUpRequest("홍길동", "hong@planit.com", "password123", "password999");

		assertThatThrownBy(() -> authService.signUp(request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_CONFIRM_MISMATCH);
	}

	@Test
	void 이메일_형식이_올바르지_않으면_예외가_발생한다() {
		SignUpRequest request = new SignUpRequest("홍길동", "not-an-email", "password123", "password123");

		assertThatThrownBy(() -> authService.signUp(request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_EMAIL_FORMAT);
	}

	@Test
	void 비밀번호가_8자_미만이면_예외가_발생한다() {
		SignUpRequest request = new SignUpRequest("홍길동", "hong@planit.com", "1234567", "1234567");

		assertThatThrownBy(() -> authService.signUp(request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_TOO_SHORT);
	}

	@Test
	void 이미_가입된_이메일이면_예외가_발생한다() {
		SignUpRequest request = new SignUpRequest("홍길동", "hong@planit.com", "password123", "password123");
		when(memberRepository.existsByEmailAndDeletedFalse("hong@planit.com")).thenReturn(true);

		assertThatThrownBy(() -> authService.signUp(request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_ALREADY_EXISTS);
	}
}
