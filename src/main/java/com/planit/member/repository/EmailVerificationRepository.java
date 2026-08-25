package com.planit.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.planit.member.entity.EmailVerification;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

	Optional<EmailVerification> findByToken(String token);
}
