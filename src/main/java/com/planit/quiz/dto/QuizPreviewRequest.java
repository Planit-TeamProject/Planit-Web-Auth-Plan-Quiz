package com.planit.quiz.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * [테스트용] 학습 계획/로그인 없이, 넘긴 학습 범위로 퀴즈 문제가 생성되는지만 확인할 때 쓰는 입력.
 * 실제 서비스 흐름은 QuizStartRequest 를 쓴다. 배포 전 이 DTO 와 관련 코드를 제거할 것.
 */
public record QuizPreviewRequest(

	/** 없으면 "테스트 과목" 으로 대체한다. */
	String subjectName,

	@NotBlank(message = "오늘 학습한 범위를 입력해 주세요")
	String todayScope
) {
}
