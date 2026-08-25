package com.planit.studyplan.entity;

import com.planit.global.BaseCreatedAtEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 04_ERD_테이블정의서 "study_plan_time_slot" 테이블.
 * 학습계획 하나에 여러 개 선택 가능한 선호 학습 시간대 보조 테이블 (REQ-B-006).
 */
@Getter
@Entity
@Table(name = "study_plan_time_slot")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyPlanTimeSlot extends BaseCreatedAtEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "study_plan_id", nullable = false)
	private StudyPlan studyPlan;

	@Enumerated(EnumType.STRING)
	@Column(name = "time_slot", nullable = false, length = 20)
	private TimeSlotType timeSlot;

	private StudyPlanTimeSlot(StudyPlan studyPlan, TimeSlotType timeSlot) {
		this.studyPlan = studyPlan;
		this.timeSlot = timeSlot;
	}

	static StudyPlanTimeSlot of(StudyPlan studyPlan, TimeSlotType timeSlot) {
		return new StudyPlanTimeSlot(studyPlan, timeSlot);
	}
}
