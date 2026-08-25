package com.planit.studyplan.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.planit.member.service.MemberDetails;
import com.planit.studyplan.dto.StudyPlanCreateRequest;
import com.planit.studyplan.dto.StudyPlanResponse;
import com.planit.studyplan.dto.TocFileUploadResponse;
import com.planit.studyplan.service.StudyPlanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 학습계획입력 API. 관련 화면: PL-001 플랜 생성 마법사 STEP1~3 (planit화면흐름도_수정.pptx)
 * 이 컨트롤러의 모든 API 는 로그인이 필요하다 (SecurityConfig 참고).
 */
@RestController
@RequestMapping("/api/study-plans")
@RequiredArgsConstructor
public class StudyPlanController {

	private final StudyPlanService studyPlanService;

	/** REQ-B-002 */
	@PostMapping("/toc-file")
	public ResponseEntity<TocFileUploadResponse> uploadTocFile(@RequestPart("file") MultipartFile file) {
		String tocFileUrl = studyPlanService.uploadTocFile(file);
		return ResponseEntity.ok(new TocFileUploadResponse(tocFileUrl));
	}

	/** REQ-B-001 ~ REQ-B-009 */
	@PostMapping
	public ResponseEntity<StudyPlanResponse> create(
		@AuthenticationPrincipal MemberDetails memberDetails,
		@Valid @RequestBody StudyPlanCreateRequest request
	) {
		StudyPlanResponse response = studyPlanService.create(memberDetails.getMember(), request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<List<StudyPlanResponse>> getMyStudyPlans(@AuthenticationPrincipal MemberDetails memberDetails) {
		return ResponseEntity.ok(studyPlanService.getMyStudyPlans(memberDetails.getMember()));
	}

	/** REQ-NF-019: 본인 학습 계획만 조회 가능 */
	@GetMapping("/{studyPlanId}")
	public ResponseEntity<StudyPlanResponse> getMyStudyPlan(
		@AuthenticationPrincipal MemberDetails memberDetails,
		@PathVariable Long studyPlanId
	) {
		return ResponseEntity.ok(studyPlanService.getMyStudyPlan(memberDetails.getMember(), studyPlanId));
	}
}
