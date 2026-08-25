package com.planit.studyplan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.planit.studyplan.entity.StudyPlan;

public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {

	List<StudyPlan> findByMemberId(Long memberId);
}
