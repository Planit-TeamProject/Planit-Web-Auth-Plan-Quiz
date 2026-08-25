package com.planit.member.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planit.global.exception.BusinessException;
import com.planit.global.exception.ErrorCode;
import com.planit.member.entity.EmailVerification;
import com.planit.member.entity.Member;
import com.planit.member.repository.EmailVerificationRepository;
import com.planit.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

/**
 * 이메일 인증 (REQ-A-006, REQ-A-015 / REQ-NF-012).
 * 화면흐름도(PPT) US-003 "이메일 인증" 화면에서 이 서비스의 verify() 를 호출한다.
 */
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

	private final EmailVerificationRepository emailVerificationRepository;
	private final MemberRepository memberRepository;
	private final EmailSender emailSender;

	@Value("${planit.email-verification.expire-minutes:30}")
	private long expireMinutes;

	/** 회원가입 완료 직후 호출된다. 새 인증 토큰을 만들고 메일을 발송한다. */
	@Transactional
	public void sendVerification(Member member) {
		EmailVerification verification = EmailVerification.issue(member, expireMinutes, LocalDateTime.now());
		emailVerificationRepository.save(verification);
		emailSender.sendVerificationEmail(member.getEmail(), verification.getToken());
	}

	/**
	 * REQ-A-015: "인증 메일 재발송" 버튼을 누르는 경우.
	 * 이메일 인증 전에는 로그인 자체가 막혀 세션이 없으므로, 이메일로 대상을 찾는다.
	 * 가입 여부가 노출되지 않도록, 존재하지 않는 이메일이어도 로그인 실패와 같은 메시지로 응답한다.
	 */
	@Transactional
	public void resendVerification(String email) {
		Member member = memberRepository.findByEmailAndDeletedFalse(email)
			.orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
		sendVerification(member);
	}

	/** 인증 링크(코드) 클릭 시 호출된다. 만료(30분) 여부를 함께 검사한다. */
	@Transactional
	public void verify(String token) {
		EmailVerification verification = emailVerificationRepository.findByToken(token)
			.orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_TOKEN_INVALID));

		if (verification.isExpired(LocalDateTime.now())) {
			throw new BusinessException(ErrorCode.VERIFICATION_TOKEN_EXPIRED);
		}

		verification.markVerified(LocalDateTime.now());
		verification.getMember().verifyEmail();
	}
}
