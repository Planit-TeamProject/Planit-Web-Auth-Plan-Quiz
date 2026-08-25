package com.planit.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.planit.quiz.entity.QuizQuestion;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
}
