package com.planit.quiz.dto;

import java.util.List;

import com.planit.quiz.entity.QuestionType;

/**
 * [테스트용] QuizQuestionGenerator 가 만든 문제를 그대로 돌려준다.
 * DB에 저장하지 않으므로 문제 id 가 없고, 프론트에서 바로 채점할 수 있게 정답 번호(answerNo)와 풀이(explanation)를 함께 담는다.
 * (실제 응시용 QuizResponse 는 제출 전까지 정답/풀이를 절대 내려주지 않는다 - 혼동 주의)
 * 배포 전 제거할 것.
 */
public record QuizPreviewResponse(
	String todayScope,
	List<Question> questions
) {
	public record Question(
		int questionNo,
		QuestionType questionType,
		String questionText,
		String choice1,
		String choice2,
		String choice3,
		String choice4,
		int answerNo,
		String explanation
	) {
	}
}
