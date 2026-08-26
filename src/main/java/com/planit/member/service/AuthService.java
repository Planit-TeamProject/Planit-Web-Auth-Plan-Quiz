package com.planit.member.service;

import java.util.regex.Pattern;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planit.global.exception.BusinessException;
import com.planit.global.exception.ErrorCode;
import com.planit.member.dto.LoginRequest;
import com.planit.member.dto.MemberResponse;
import com.planit.member.dto.SignUpRequest;
import com.planit.member.entity.Member;
import com.planit.member.repository.MemberRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 회원가입 / 로그인 / 로그아웃 (REQ-A-001 ~ REQ-A-015).
 *
 * 각 메서드는 02_요구사항정의서.xlsx 에 적힌 순서 그대로 조건을 검사한다.
 * 순서를 바꾸면 요구사항정의서와 어긋나므로, 검증 순서를 바꿔야 한다면 문서도 함께 갱신할 것.
 *
 * 회원가입/로그인/로그아웃의 성공·실패는 [AUTH] 태그를 붙여 로그로 남긴다. (REQ-NF-016)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	// REQ-A-003: 이메일 형식 검증 (예: user@domain.com)
	private static final Pattern EMAIL_PATTERN =
		Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[A-Za-z]{2,}$");

	private static final int MIN_PASSWORD_LENGTH = 8; // REQ-A-004

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailVerificationService emailVerificationService;
	private final AuthenticationManager authenticationManager;
	private final LoginFailureRecorder loginFailureRecorder;

	/** 회원가입 (REQ-A-001 ~ REQ-A-006) */
	@Transactional
	public MemberResponse signUp(SignUpRequest request) {

		// REQ-A-002: 비밀번호 확인 일치 검증
		if (!request.password().equals(request.passwordConfirm())) {
			throw new BusinessException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
		}

		// REQ-A-003: 이메일 형식 검증
		if (!EMAIL_PATTERN.matcher(request.email()).matches()) {
			throw new BusinessException(ErrorCode.INVALID_EMAIL_FORMAT);
		}

		// REQ-A-004: 비밀번호 길이 검증
		if (request.password().length() < MIN_PASSWORD_LENGTH) {
			throw new BusinessException(ErrorCode.PASSWORD_TOO_SHORT);
		}

		// REQ-A-005: 이메일 중복 검사 (email 컬럼 UNIQUE 제약이 최종 방어선 - REQ-NF-010)
		if (memberRepository.existsByEmailAndDeletedFalse(request.email())) {
			throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
		}

		Member member = Member.create(request.email(), passwordEncoder.encode(request.password()), request.name());
		memberRepository.save(member);

		// REQ-A-006: 회원가입 완료 시 인증 메일 발송, 인증 전까지 로그인 제한
		// [임시] 이메일 인증 비활성화 - 인증 메일 발송 주석 처리 (원복 시 아래 줄 복구)
		// emailVerificationService.sendVerification(member);

		log.info("[AUTH] 회원가입 완료 - email={}, name={}", member.getEmail(), member.getName());
		return MemberResponse.from(member);
	}

	/**
	 * 로그인 (REQ-A-008 ~ REQ-A-011, REQ-A-015).
	 *
	 * 실제 인증 판단은 Spring Security 표준 흐름(AuthenticationManager -> DaoAuthenticationProvider
	 * -> MemberDetailsService/PasswordEncoder)에 맡긴다. MemberDetails.isAccountNonLocked()/isEnabled() 값에 따라
	 * 잠김(LockedException)/미인증(DisabledException)이 비밀번호 검사보다 먼저 판정되고,
	 * 이메일 자체가 없거나 비밀번호가 틀리면 BadCredentialsException 으로 합쳐져서 넘어온다
	 * (DaoAuthenticationProvider 의 hideUserNotFoundExceptions 기본값이 true 라 REQ-A-009 문구를 그대로 쓸 수 있다).
	 */
	@Transactional
	public MemberResponse login(LoginRequest request, HttpServletRequest servletRequest) {
		Authentication authentication;
		try {
			authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.email(), request.password()));
		} catch (LockedException e) {
			// REQ-NF-013
			log.warn("[AUTH] 로그인 실패(계정 잠김) - email={}", request.email());
			throw new BusinessException(ErrorCode.LOGIN_LOCKED);
		} catch (DisabledException e) {
			// REQ-A-015
			log.warn("[AUTH] 로그인 실패(이메일 미인증) - email={}", request.email());
			throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
		} catch (BadCredentialsException e) {
			// REQ-A-009 + REQ-NF-013 (실패 횟수 누적)
			loginFailureRecorder.record(request.email());
			log.warn("[AUTH] 로그인 실패(이메일 없음 또는 비밀번호 불일치) - email={}", request.email());
			throw new BusinessException(ErrorCode.LOGIN_FAILED);
		}

		MemberDetails memberDetails = (MemberDetails) authentication.getPrincipal();
		memberDetails.getMember().resetFailedLoginCount();

		// REQ-A-011: 세션 발급 (로그인 상태 유지)
		persistLoginSession(authentication, servletRequest);

		log.info("[AUTH] 로그인 성공 - email={}", memberDetails.getMember().getEmail());
		return MemberResponse.from(memberDetails.getMember());
	}

	/** 로그아웃 (REQ-A-012) */
	public void logout(HttpServletRequest request, HttpServletResponse response) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = (authentication != null) ? authentication.getName() : "(비로그인)";

		new SecurityContextLogoutHandler().logout(request, response, authentication);

		log.info("[AUTH] 로그아웃 완료 - email={}", email);
	}

	/** 인증된 Authentication 을 SecurityContext 에 담아 HttpSession 에 저장한다 (REQ-A-011). */
	private void persistLoginSession(Authentication authentication, HttpServletRequest servletRequest) {
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);

		servletRequest.getSession(true)
			.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
	}
}
