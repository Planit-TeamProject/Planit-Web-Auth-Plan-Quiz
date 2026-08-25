package com.planit.member.service;

/**
 * 실제 메일 발송 방식(SMTP, AWS SES 등)을 감추기 위한 인터페이스.
 * 팀에서 발송 방식을 정하면 이 인터페이스의 구현체만 새로 만들어 갈아끼우면 된다.
 * 기본으로는 콘솔/로그로만 출력하는 LoggingEmailSender 를 사용한다 (application.yml 에 메일 서버 정보가 없어도 바로 실행 가능).
 */
public interface EmailSender {

	void sendVerificationEmail(String toEmail, String verificationToken);
}
