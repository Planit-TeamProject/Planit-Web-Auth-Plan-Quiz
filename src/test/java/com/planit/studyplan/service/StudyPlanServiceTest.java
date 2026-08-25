package com.planit.studyplan.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.planit.global.exception.BusinessException;
import com.planit.global.exception.ErrorCode;
import com.planit.member.entity.Member;
import com.planit.studyplan.dto.StudyPlanCreateRequest;
import com.planit.studyplan.repository.StudyPlanRepository;

/** 학습 계획 생성 검증(REQ-B-003, REQ-B-005)에 대한 단위 테스트 */
@ExtendWith(MockitoExtension.class)
class StudyPlanServiceTest {

	@Mock
	private StudyPlanRepository studyPlanRepository;

	@Mock
	private FileStorageService fileStorageService;

	@InjectMocks
	private StudyPlanService studyPlanService;

	private final Member member = Member.create("test@planit.com", "encoded", "테스터");

	@Test
	void 과목명이_없으면_예외가_발생한다() {
		StudyPlanCreateRequest request = new StudyPlanCreateRequest(
			"", "toc/sample.pdf", LocalDate.now(), LocalDate.now().plusDays(30), 60, null);

		assertThatThrownBy(() -> studyPlanService.create(member, request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_PLAN_STEP1_REQUIRED);
	}

	@Test
	void 목차파일이_없으면_예외가_발생한다() {
		StudyPlanCreateRequest request = new StudyPlanCreateRequest(
			"정보처리기사", null, LocalDate.now(), LocalDate.now().plusDays(30), 60, null);

		assertThatThrownBy(() -> studyPlanService.create(member, request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_PLAN_STEP1_REQUIRED);
	}

	@Test
	void 종료일이_시작일보다_빠르면_예외가_발생한다() {
		LocalDate start = LocalDate.now();
		LocalDate end = start.minusDays(1);
		StudyPlanCreateRequest request =
			new StudyPlanCreateRequest("정보처리기사", "toc/sample.pdf", start, end, 60, null);

		assertThatThrownBy(() -> studyPlanService.create(member, request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_PLAN_PERIOD_INVALID);
	}
}
