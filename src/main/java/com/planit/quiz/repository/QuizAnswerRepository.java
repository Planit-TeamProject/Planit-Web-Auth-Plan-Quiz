package com.planit.quiz.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.planit.quiz.entity.QuizAnswer;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {

	Optional<QuizAnswer> findByQuizQuestionIdAndMemberId(Long quizQuestionId, Long memberId);

	List<QuizAnswer> findByQuizQuestionQuizIdAndMemberId(Long quizId, Long memberId);
}
