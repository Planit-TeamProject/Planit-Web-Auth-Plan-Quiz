package com.planit.member.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.planit.member.entity.Member;

import lombok.Getter;

/**
 * Spring Security 가 인증/인가에 사용하는 사용자 정보 래퍼.
 * isEnabled() 를 이메일 인증 여부와 연결해서, 인증 안 된 계정은 Security 단계에서부터 로그인이 막히게 한다. (REQ-A-015)
 */
@Getter
public class MemberDetails implements UserDetails {

	private final Member member;

	public MemberDetails(Member member) {
		this.member = member;
	}

	public Long getMemberId() {
		return member.getId();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_MEMBER"));
	}

	@Override
	public String getPassword() {
		return member.getPassword();
	}

	@Override
	public String getUsername() {
		return member.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// REQ-NF-013: 5회 연속 로그인 실패 시 1분간 로그인 차단.
		// AuthenticationManager(DaoAuthenticationProvider) 가 비밀번호 검사보다 먼저 이 값을 확인해서
		// 잠겨 있으면 LockedException 을 던진다 (AuthService#login 참고).
		return !member.isLocked(LocalDateTime.now());
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		// [임시] 이메일 인증 없이 로그인 허용하도록 주석 처리 (원복 시 아래 줄 복구)
		// return member.isEmailVerified();
		return true;
	}
}
