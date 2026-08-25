package com.planit.studyplan.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** REQ-B-006: 선호 학습 시간대 (복수 선택 가능) */
@Getter
@RequiredArgsConstructor
public enum TimeSlotType {

	EARLY_MORNING("아침(6~9시)"),
	MORNING("오전(9~12시)"),
	AFTERNOON("오후(1~6시)"),
	EVENING("저녁(6~10시)"),
	LATE_NIGHT("심야(10~12시)"),
	WEEKEND_MORNING("주말 오전"),
	WEEKEND_AFTERNOON("주말 오후"),
	WEEKDAY_ONLY("평일만");

	private final String description;
}
