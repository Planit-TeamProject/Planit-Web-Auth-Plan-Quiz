package com.planit.quiz.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planit.member.service.MemberDetails;
import com.planit.quiz.dto.QuizResponse;
import com.planit.quiz.dto.QuizResultSummaryResponse;
import com.planit.quiz.dto.QuizStartRequest;
import com.planit.quiz.dto.QuizSubmitRequest;
import com.planit.quiz.dto.QuizSubmitResponse;
import com.planit.quiz.service.QuizService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 퀴즈봇 API. 관련 화면: QZ-001 (planit화면흐름도_수정.pptx)
 * 이 컨트롤러의 모든 API 는 로그인이 필요하다 (SecurityConfig 참고).
 */
@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

	private final QuizService quizService;

	/** REQ-Q-001 ~ REQ-Q-003 */
	@PostMapping
	public ResponseEntity<QuizResponse> start(
		@AuthenticationPrincipal MemberDetails memberDetails,
		@Valid @RequestBody QuizStartRequest request
	) {
		QuizResponse response = quizService.start(memberDetails.getMember(), request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{quizId}")
	public ResponseEntity<QuizResponse> getQuiz(
		@AuthenticationPrincipal MemberDetails memberDetails,
		@PathVariable Long quizId
	) {
		return ResponseEntity.ok(quizService.getQuiz(memberDetails.getMember(), quizId));
	}

	/** REQ-Q-004, REQ-Q-005 */
	@PostMapping("/questions/{questionId}/answers")
	public ResponseEntity<QuizSubmitResponse> submit(
		@AuthenticationPrincipal MemberDetails memberDetails,
		@PathVariable Long questionId,
		@Valid @RequestBody QuizSubmitRequest request
	) {
		QuizSubmitResponse response =
			quizService.submit(memberDetails.getMember(), questionId, request.selectedChoice());
		return ResponseEntity.ok(response);
	}

	/** REQ-Q-006 */
	@GetMapping("/{quizId}/summary")
	public ResponseEntity<QuizResultSummaryResponse> getResultSummary(
		@AuthenticationPrincipal MemberDetails memberDetails,
		@PathVariable Long quizId
	) {
		return ResponseEntity.ok(quizService.getResultSummary(memberDetails.getMember(), quizId));
	}
}
