package com.planit.member.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.planit.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

/**
 * REQ-NF-013: 로그인 5회 연속 실패 시 1분간 로그인 차단.
 *
 * AuthService#login() 은 로그인 실패 시 BusinessException 을 던져 그 자리에서 자기 트랜잭션을 롤백한다.
 * 그 상태에서 실패 횟수 증가를 AuthService 안의 같은 메서드로 처리하면(self-invocation) 프록시를 거치지
 * 않아 REQUIRES_NEW 가 적용되지 않고, 실패 횟수 증가 자체가 롤백되어 사라진다. 그래서 별도 빈으로 분리했다.
 */
@Component
@RequiredArgsConstructor
public class LoginFailureRecorder {

	private final MemberRepository memberRepository;

	@Value("${planit.login.max-fail-count:5}")
	private int maxFailCount;

	@Value("${planit.login.lock-minutes:1}")
	private long lockMinutes;

	/** 이 메서드만 별도 트랜잭션으로 즉시 커밋되어, 호출한 쪽이 이어서 예외를 던지고 롤백해도 영향받지 않는다. */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void record(String email) {
		memberRepository.findByEmailAndDeletedFalse(email)
			.ifPresent(member -> member.increaseFailedLoginCount(maxFailCount, lockMinutes, LocalDateTime.now()));
	}
}
