package com.planit.member.dto;

import com.planit.member.entity.Member;

/** 회원가입 성공 / 로그인 성공 / 내 정보 조회에 공통으로 쓰는 응답 (비밀번호는 절대 포함하지 않는다) */
public record MemberResponse(
	Long id,
	String email,
	String name,
	boolean emailVerified
) {
	public static MemberResponse from(Member member) {
		return new MemberResponse(member.getId(), member.getEmail(), member.getName(), member.isEmailVerified());
	}
}
