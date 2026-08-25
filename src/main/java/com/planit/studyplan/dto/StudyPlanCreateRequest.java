package com.planit.studyplan.dto;

import java.time.LocalDate;
import java.util.List;

import com.planit.studyplan.entity.TimeSlotType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 플랜 생성 마법사 STEP1(과목/목차) + STEP2(기간/시간대) 입력값 (REQ-B-001, 004, 006, 007).
 * tocFileUrl 은 미리 POST /api/study-plans/toc-file 로 업로드한 뒤 받은 값을 그대로 넣는다.
 */
public record StudyPlanCreateRequest(

	@NotBlank(message = "학습할 과목명 또는 자격증명을 입력해 주세요")
	String subjectName,

	String tocFileUrl,

	@NotNull(message = "학습 시작일을 입력해 주세요")
	LocalDate startDate,

	@NotNull(message = "종료일(시험일)을 입력해 주세요")
	LocalDate endDate,

	Integer dailyAvailableMinutes,

	List<TimeSlotType> timeSlots
) {
}
