package com.planit.quiz.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planit.global.exception.BusinessException;
import com.planit.global.exception.ErrorCode;
import com.planit.member.entity.Member;
import com.planit.quiz.dto.QuizResponse;
import com.planit.quiz.dto.QuizResultSummaryResponse;
import com.planit.quiz.dto.QuizStartRequest;
import com.planit.quiz.dto.QuizSubmitResponse;
import com.planit.quiz.entity.Quiz;
import com.planit.quiz.entity.QuizAnswer;
import com.planit.quiz.entity.QuizQuestion;
import com.planit.quiz.repository.QuizAnswerRepository;
import com.planit.quiz.repository.QuizQuestionRepository;
import com.planit.quiz.repository.QuizRepository;
import com.planit.studyplan.entity.StudyPlan;
import com.planit.studyplan.repository.StudyPlanRepository;

import lombok.RequiredArgsConstructor;

/**
 * 퀴즈봇 (REQ-Q-001 ~ REQ-Q-006). 관련 화면: QZ-001 (planit화면흐름도_수정.pptx)
 */
@Service
@RequiredArgsConstructor
public class QuizService {

	private final QuizRepository quizRepository;
	private final QuizQuestionRepository quizQuestionRepository;
	private final QuizAnswerRepository quizAnswerRepository;
	private final StudyPlanRepository studyPlanRepository;
	private final QuizQuestionGenerator quizQuestionGenerator;

	/** REQ-Q-001 ~ REQ-Q-003: 오늘 체크리스트 100% 완료 시 호출되어 퀴즈를 생성한다. */
	@Transactional
	public QuizResponse start(Member member, QuizStartRequest request) {
		StudyPlan studyPlan = studyPlanRepository.findById(request.studyPlanId())
			.orElseThrow(() -> new BusinessException(ErrorCode.STUDY_PLAN_NOT_FOUND));

		if (!studyPlan.isOwnedBy(member)) {
			throw new BusinessException(ErrorCode.QUIZ_ACCESS_DENIED);
		}

		LocalDate today = LocalDate.now();

		// ERD 비고: 회원+학습계획+날짜 조합으로 하루 1세트만 생성되게 한다.
		if (quizRepository.existsByMemberIdAndStudyPlanIdAndQuizDate(member.getId(), studyPlan.getId(), today)) {
			throw new BusinessException(ErrorCode.QUIZ_ALREADY_EXISTS_FOR_TODAY);
		}

		Quiz quiz = Quiz.create(member, studyPlan, today);

		// REQ-Q-002, REQ-Q-003: 오늘 학습 범위 안에서 BASIC 2문제 + APPLIED 1문제 생성
		List<GeneratedQuestion> generatedQuestions =
			quizQuestionGenerator.generate(studyPlan.getSubjectName(), request.todayScope());

		int questionNo = 1;
		for (GeneratedQuestion g : generatedQuestions) {
			quiz.addQuestion(new QuizQuestion(
				quiz, questionNo++, g.questionType(), g.questionText(),
				g.choice1(), g.choice2(), g.choice3(), g.choice4(), g.answerNo(), g.explanation()
			));
		}

		quizRepository.save(quiz);
		return QuizResponse.from(quiz);
	}

	@Transactional(readOnly = true)
	public QuizResponse getQuiz(Member member, Long quizId) {
		Quiz quiz = findAndValidateOwner(member, quizId);
		return QuizResponse.from(quiz);
	}

	/** REQ-Q-004, REQ-Q-005: 문제 하나를 제출하고, 그 자리에서 채점 결과와 풀이를 돌려준다. */
	@Transactional
	public QuizSubmitResponse submit(Member member, Long questionId, int selectedChoice) {
		// 컨트롤러 단에서 QuizSubmitRequest 의 @Min/@Max 로 이미 걸러지지만,
		// 서비스가 다른 곳(배치, 관리자 도구 등)에서도 재사용될 수 있어 한 번 더 방어한다.
		if (selectedChoice < 1 || selectedChoice > 4) {
			throw new BusinessException(ErrorCode.QUIZ_CHOICE_OUT_OF_RANGE);
		}

		QuizQuestion question = quizQuestionRepository.findById(questionId)
			.orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_QUESTION_NOT_FOUND));

		if (!question.getQuiz().isOwnedBy(member)) {
			throw new BusinessException(ErrorCode.QUIZ_ACCESS_DENIED);
		}

		// REQ-NF-015 관련(ERD 비고): 문제 하나당 1번만 제출 가능
		quizAnswerRepository.findByQuizQuestionIdAndMemberId(questionId, member.getId())
			.ifPresent(existing -> {
				throw new BusinessException(ErrorCode.QUIZ_ALREADY_ANSWERED);
			});

		QuizAnswer answer = QuizAnswer.submit(question, member, selectedChoice);
		quizAnswerRepository.save(answer);

		return QuizSubmitResponse.of(question, answer);
	}

	/** REQ-Q-006: 3문제를 모두 제출하면 맞힌 문제 수를 요약해 표시한다. */
	@Transactional(readOnly = true)
	public QuizResultSummaryResponse getResultSummary(Member member, Long quizId) {
		Quiz quiz = findAndValidateOwner(member, quizId);

		List<QuizAnswer> answers = quizAnswerRepository.findByQuizQuestionQuizIdAndMemberId(quizId, member.getId());
		long correctCount = answers.stream().filter(QuizAnswer::isCorrect).count();

		return new QuizResultSummaryResponse(
			quiz.getId(),
			quiz.getQuestions().size(),
			answers.size(),
			(int) correctCount
		);
	}

	private Quiz findAndValidateOwner(Member member, Long quizId) {
		Quiz quiz = quizRepository.findById(quizId)
			.orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

		if (!quiz.isOwnedBy(member)) {
			throw new BusinessException(ErrorCode.QUIZ_ACCESS_DENIED);
		}
		return quiz;
	}
}
