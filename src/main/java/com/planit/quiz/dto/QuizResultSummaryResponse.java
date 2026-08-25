package com.planit.quiz.dto;

/** REQ-Q-006: 3문제를 모두 제출하면 맞힌 문제 수를 요약해 보여준다. */
public record QuizResultSummaryResponse(
	Long quizId,
	int totalQuestionCount,
	int answeredCount,
	int correctCount
) {
}
