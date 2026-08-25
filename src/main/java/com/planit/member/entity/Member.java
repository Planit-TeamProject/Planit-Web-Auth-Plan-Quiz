package com.planit.member.entity;

import java.time.LocalDateTime;

import com.planit.global.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 04_ERD_테이블정의서 "member" 테이블 (김동호 담당 - 회원가입/로그인).
 * 이메일을 로그인 ID로 사용한다. (REQ-A-001, REQ-A-008)
 */
@Getter
@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	/** BCrypt 로 암호화된 값만 저장한다. (REQ-NF-009) */
	@Column(nullable = false, length = 255)
	private String password;

	@Column(nullable = false, length = 30)
	private String name;

	/** 이메일 인증 전에는 로그인을 막는다. (REQ-A-006, REQ-A-015) */
	@Column(name = "is_email_verified", nullable = false)
	private boolean emailVerified = false;

	/** 5회 연속 로그인 실패 시 1분간 로그인을 막는다. (REQ-NF-013) */
	@Column(name = "failed_login_count", nullable = false)
	private int failedLoginCount = 0;

	@Column(name = "locked_until")
	private LocalDateTime lockedUntil;

	/** 탈퇴 시 실제로 삭제하지 않고 이 값만 켠다. (공통 설계 규칙 - 논리 삭제) */
	@Column(name = "is_deleted", nullable = false)
	private boolean deleted = false;

	public Member(String email, String encodedPassword, String name) {
		this.email = email;
		this.password = encodedPassword;
		this.name = name;
	}

	public static Member create(String email, String encodedPassword, String name) {
		return new Member(email, encodedPassword, name);
	}

	public void verifyEmail() {
		this.emailVerified = true;
	}

	public boolean isLocked(LocalDateTime now) {
		return lockedUntil != null && lockedUntil.isAfter(now);
	}

	/** 로그인 실패 처리. maxFailCount 에 도달하면 lockMinutes 동안 잠근다. (REQ-NF-013) */
	public void increaseFailedLoginCount(int maxFailCount, long lockMinutes, LocalDateTime now) {
		this.failedLoginCount++;
		if (this.failedLoginCount >= maxFailCount) {
			this.lockedUntil = now.plusMinutes(lockMinutes);
		}
	}

	public void resetFailedLoginCount() {
		this.failedLoginCount = 0;
		this.lockedUntil = null;
	}
}
