package com.planit.member.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.planit.global.BaseCreatedAtEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 04_ERD_테이블정의서 "email_verification" 테이블.
 * 회원가입 시 발송하는 인증 메일의 토큰과 만료 시각을 관리한다. (REQ-A-006, REQ-A-015 / REQ-NF-012)
 *
 * 참고: 화면흐름도(PPT) US-003 "이메일 인증" 화면이 이 테이블 데이터를 사용한다.
 */
@Getter
@Entity
@Table(name = "email_verification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification extends BaseCreatedAtEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Column(nullable = false, unique = true, length = 255)
	private String token;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "verified_at")
	private LocalDateTime verifiedAt;

	private EmailVerification(Member member, String token, LocalDateTime expiresAt) {
		this.member = member;
		this.token = token;
		this.expiresAt = expiresAt;
	}

	/** expireMinutes 뒤에 만료되는 새 인증 토큰을 발급한다. (REQ-NF-012: 기본 30분) */
	public static EmailVerification issue(Member member, long expireMinutes, LocalDateTime now) {
		String token = UUID.randomUUID().toString();
		return new EmailVerification(member, token, now.plusMinutes(expireMinutes));
	}

	public boolean isExpired(LocalDateTime now) {
		return expiresAt.isBefore(now);
	}

	public boolean isVerified() {
		return verifiedAt != null;
	}

	public void markVerified(LocalDateTime now) {
		this.verifiedAt = now;
	}
}
