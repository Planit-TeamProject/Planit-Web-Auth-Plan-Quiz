package com.planit.studyplan.dto;

import java.time.LocalDate;
import java.util.List;

import com.planit.studyplan.entity.StudyPlan;
import com.planit.studyplan.entity.TimeSlotType;

public record StudyPlanResponse(
	Long id,
	String subjectName,
	String tocFileUrl,
	LocalDate startDate,
	LocalDate endDate,
	Integer dailyAvailableMinutes,
	List<TimeSlotType> timeSlots
) {
	public static StudyPlanResponse from(StudyPlan studyPlan) {
		List<TimeSlotType> timeSlots = studyPlan.getTimeSlots().stream()
			.map(slot -> slot.getTimeSlot())
			.toList();

		return new StudyPlanResponse(
			studyPlan.getId(),
			studyPlan.getSubjectName(),
			studyPlan.getTocFileUrl(),
			studyPlan.getStartDate(),
			studyPlan.getEndDate(),
			studyPlan.getDailyAvailableMinutes(),
			timeSlots
		);
	}
}
