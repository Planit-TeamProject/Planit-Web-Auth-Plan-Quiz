package com.planit.quiz.dto;

import java.time.LocalDate;
import java.util.List;

import com.planit.quiz.entity.Quiz;

public record QuizResponse(
	Long id,
	LocalDate quizDate,
	List<QuizQuestionResponse> questions
) {
	public static QuizResponse from(Quiz quiz) {
		List<QuizQuestionResponse> questions = quiz.getQuestions().stream()
			.map(QuizQuestionResponse::from)
			.toList();
		return new QuizResponse(quiz.getId(), quiz.getQuizDate(), questions);
	}
}
