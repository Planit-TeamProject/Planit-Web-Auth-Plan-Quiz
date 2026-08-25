package com.planit.quiz.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** REQ-Q-005: 제출한 보기가 정답 번호와 같은지 채점하는 로직 */
class QuizQuestionTest {

	private final QuizQuestion question = new QuizQuestion(
		null, 1, QuestionType.BASIC, "질문 내용",
		"보기1", "보기2", "보기3", "보기4",
		3, "풀이 내용"
	);

	@Test
	void 정답_번호와_같은_보기를_선택하면_정답이다() {
		assertThat(question.isCorrect(3)).isTrue();
	}

	@Test
	void 정답_번호와_다른_보기를_선택하면_오답이다() {
		assertThat(question.isCorrect(1)).isFalse();
	}
}
