package com.planit.studyplan.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.planit.global.exception.BusinessException;
import com.planit.global.exception.ErrorCode;
import com.planit.member.entity.Member;
import com.planit.studyplan.dto.StudyPlanCreateRequest;
import com.planit.studyplan.dto.StudyPlanResponse;
import com.planit.studyplan.entity.StudyPlan;
import com.planit.studyplan.entity.TimeSlotType;
import com.planit.studyplan.repository.StudyPlanRepository;

import lombok.RequiredArgsConstructor;

/**
 * 학습계획입력 (REQ-B-001 ~ REQ-B-011).
 * 관련 화면: PL-001 플랜 생성 마법사 STEP1~3 (planit화면흐름도_수정.pptx)
 *
 * STEP1/STEP2 화면 전환과 입력값 유지(REQ-B-010)는 프론트엔드가 담당하고,
 * 백엔드는 STEP1~2 값을 한 번에 받아 저장(REQ-B-009)하는 API 하나로 제공한다.
 */
@Service
@RequiredArgsConstructor
public class StudyPlanService {

	private final StudyPlanRepository studyPlanRepository;
	private final FileStorageService fileStorageService;

	/** REQ-B-002: 목차 파일 업로드 (PDF/JPG/PNG, 최대 10MB 는 application.yml multipart 설정에서 막는다) */
	public String uploadTocFile(MultipartFile file) {
		return fileStorageService.store(file);
	}

	/** REQ-B-001 ~ REQ-B-009: 학습 계획 생성 */
	@Transactional
	public StudyPlanResponse create(Member member, StudyPlanCreateRequest request) {

		// REQ-B-003: 과목명과 목차 파일 중 하나라도 없으면 저장하지 않는다
		if (isBlank(request.subjectName()) || isBlank(request.tocFileUrl())) {
			throw new BusinessException(ErrorCode.STUDY_PLAN_STEP1_REQUIRED);
		}

		// REQ-B-005: 종료일(시험일)은 시작일보다 빠를 수 없다
		if (request.endDate().isBefore(request.startDate())) {
			throw new BusinessException(ErrorCode.STUDY_PLAN_PERIOD_INVALID);
		}

		StudyPlan studyPlan = StudyPlan.create(
			member,
			request.subjectName(),
			request.tocFileUrl(),
			request.startDate(),
			request.endDate(),
			request.dailyAvailableMinutes()
		);

		// REQ-B-006: 선호 학습 시간대 복수 선택
		List<TimeSlotType> timeSlots = request.timeSlots();
		if (timeSlots != null) {
			timeSlots.forEach(studyPlan::addTimeSlot);
		}

		studyPlanRepository.save(studyPlan);
		return StudyPlanResponse.from(studyPlan);
	}

	@Transactional(readOnly = true)
	public StudyPlanResponse getMyStudyPlan(Member member, Long studyPlanId) {
		StudyPlan studyPlan = findAndValidateOwner(member, studyPlanId);
		return StudyPlanResponse.from(studyPlan);
	}

	@Transactional(readOnly = true)
	public List<StudyPlanResponse> getMyStudyPlans(Member member) {
		return studyPlanRepository.findByMemberId(member.getId()).stream()
			.map(StudyPlanResponse::from)
			.toList();
	}

	/** REQ-NF-019: 본인이 등록한 학습 계획만 조회/수정할 수 있다. */
	private StudyPlan findAndValidateOwner(Member member, Long studyPlanId) {
		StudyPlan studyPlan = studyPlanRepository.findById(studyPlanId)
			.orElseThrow(() -> new BusinessException(ErrorCode.STUDY_PLAN_NOT_FOUND));

		if (!studyPlan.isOwnedBy(member)) {
			throw new BusinessException(ErrorCode.STUDY_PLAN_ACCESS_DENIED);
		}
		return studyPlan;
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
