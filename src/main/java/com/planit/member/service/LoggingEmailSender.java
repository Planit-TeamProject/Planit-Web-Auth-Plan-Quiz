package com.planit.member.service;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 개발 단계용 기본 구현체. 실제 메일을 보내지 않고 로그로만 인증 링크를 출력한다.
 * 운영 배포 전 팀 논의 후 실제 메일 발송(SMTP/SES 등) 구현체로 교체할 것.
 */
@Slf4j
@Component
public class LoggingEmailSender implements EmailSender {

	@Override
	public void sendVerificationEmail(String toEmail, String verificationToken) {
		log.info("[이메일 인증 메일 발송 - 개발용 로그 출력] to={}, verifyUrl=/api/auth/email-verification?token={}",
			toEmail, verificationToken);
	}
}
