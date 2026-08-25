package com.planit.member.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/** REQ-NF-013: 로그인 5회 연속 실패 시 1분간 로그인 차단 */
class MemberTest {

	private static final int MAX_FAIL_COUNT = 5;
	private static final long LOCK_MINUTES = 1;

	@Test
	void 로그인_실패가_5회_미만이면_잠기지_않는다() {
		Member member = Member.create("test@planit.com", "encoded-password", "테스터");
		LocalDateTime now = LocalDateTime.now();

		for (int i = 0; i < MAX_FAIL_COUNT - 1; i++) {
			member.increaseFailedLoginCount(MAX_FAIL_COUNT, LOCK_MINUTES, now);
		}

		assertThat(member.isLocked(now)).isFalse();
	}

	@Test
	void 로그인_실패가_5회가_되면_1분간_잠긴다() {
		Member member = Member.create("test@planit.com", "encoded-password", "테스터");
		LocalDateTime now = LocalDateTime.now();

		for (int i = 0; i < MAX_FAIL_COUNT; i++) {
			member.increaseFailedLoginCount(MAX_FAIL_COUNT, LOCK_MINUTES, now);
		}

		assertThat(member.isLocked(now)).isTrue();
		assertThat(member.isLocked(now.plusMinutes(2))).isFalse();
	}

	@Test
	void 로그인에_성공하면_실패_횟수가_초기화된다() {
		Member member = Member.create("test@planit.com", "encoded-password", "테스터");
		LocalDateTime now = LocalDateTime.now();

		member.increaseFailedLoginCount(MAX_FAIL_COUNT, LOCK_MINUTES, now);
		member.increaseFailedLoginCount(MAX_FAIL_COUNT, LOCK_MINUTES, now);
		member.resetFailedLoginCount();

		assertThat(member.getFailedLoginCount()).isZero();
		assertThat(member.isLocked(now)).isFalse();
	}
}
