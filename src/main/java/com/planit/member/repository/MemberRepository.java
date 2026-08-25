package com.planit.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.planit.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByEmailAndDeletedFalse(String email);

	boolean existsByEmailAndDeletedFalse(String email);
}
