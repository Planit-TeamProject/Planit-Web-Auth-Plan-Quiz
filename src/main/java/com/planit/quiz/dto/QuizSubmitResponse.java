package com.planit.quiz.dto;

import com.planit.quiz.entity.QuizAnswer;
import com.planit.quiz.entity.QuizQuestion;

/** REQ-Q-005: 제출하면 해당 문제 바로 아래에 정답 여부와 풀이를 보여준다. */
public record QuizSubmitResponse(
	Long questionId,
	boolean correct,
	int answerNo,
	String explanation
) {
	public static QuizSubmitResponse of(QuizQuestion question, QuizAnswer answer) {
		return new QuizSubmitResponse(question.getId(), answer.isCorrect(), question.getAnswerNo(), question.getExplanation());
	}
}
