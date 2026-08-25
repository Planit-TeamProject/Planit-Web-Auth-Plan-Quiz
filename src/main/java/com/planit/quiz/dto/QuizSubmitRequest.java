package com.planit.quiz.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** REQ-Q-004: 4개 보기 중 하나를 선택해 문제별로 제출한다. */
public record QuizSubmitRequest(

	@NotNull(message = "보기를 선택해 주세요")
	@Min(value = 1, message = "보기 번호는 1~4 중 하나여야 합니다")
	@Max(value = 4, message = "보기 번호는 1~4 중 하나여야 합니다")
	Integer selectedChoice
) {
}
