package com.planit.quiz.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.planit.quiz.entity.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

	Optional<Quiz> findByMemberIdAndStudyPlanIdAndQuizDate(Long memberId, Long studyPlanId, LocalDate quizDate);

	boolean existsByMemberIdAndStudyPlanIdAndQuizDate(Long memberId, Long studyPlanId, LocalDate quizDate);
}
