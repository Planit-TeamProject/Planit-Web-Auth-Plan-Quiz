package com.planit.quiz.dto;

import com.planit.quiz.entity.QuestionType;
import com.planit.quiz.entity.QuizQuestion;

/** 응시 중 화면에 보여주는 문제. 정답/풀이는 제출 전까지 절대 내려주지 않는다 (REQ-Q-005 는 "제출 후"에만 공개). */
public record QuizQuestionResponse(
	Long id,
	int questionNo,
	QuestionType questionType,
	String questionText,
	String choice1,
	String choice2,
	String choice3,
	String choice4
) {
	public static QuizQuestionResponse from(QuizQuestion question) {
		return new QuizQuestionResponse(
			question.getId(),
			question.getQuestionNo(),
			question.getQuestionType(),
			question.getQuestionText(),
			question.getChoice1(),
			question.getChoice2(),
			question.getChoice3(),
			question.getChoice4()
		);
	}
}
