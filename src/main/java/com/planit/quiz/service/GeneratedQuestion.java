package com.planit.quiz.service;

import com.planit.quiz.entity.QuestionType;

/**
 * QuizQuestionGenerator 가 만들어내는 문제 1개를 담는 값 객체.
 * AI(OpenAI API)가 만들든, 미리 준비한 문제 은행에서 고르든 이 형태로만 맞춰주면 QuizService 가 그대로 사용할 수 있다.
 */
public record GeneratedQuestion(
	QuestionType questionType,
	String questionText,
	String choice1,
	String choice2,
	String choice3,
	String choice4,
	int answerNo,       // 1~4
	String explanation
) {
}
