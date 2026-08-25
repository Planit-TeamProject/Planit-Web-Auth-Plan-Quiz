package com.planit.quiz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * REQ-Q-001: 오늘 학습 체크리스트를 100% 완료했을 때 프론트에서 호출한다.
 *
 * 참고(확인 필요 항목 #3): "체크리스트 100% 완료" 판정 자체는 유시우 담당(학습 계획 조회/체크) 모듈의 일이라,
 * 이 API 는 그 판정이 끝난 뒤 프론트가 호출해 주는 것을 전제로 한다. todayScope 는 오늘 완료 처리된
 * 학습 범위(단원/챕터 설명)를 그대로 넘겨받아 출제 범위를 제한하는 데 사용한다 (REQ-Q-003).
 */
public record QuizStartRequest(

	@NotNull(message = "학습 계획을 선택해 주세요")
	Long studyPlanId,

	@NotBlank(message = "오늘 학습한 범위를 입력해 주세요")
	String todayScope
) {
}
