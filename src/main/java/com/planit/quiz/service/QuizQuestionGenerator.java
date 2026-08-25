package com.planit.quiz.service;

import java.util.List;

/**
 * 오늘 학습 범위를 바탕으로 퀴즈 문제를 만드는 방법을 감추는 인터페이스 (REQ-Q-002, REQ-Q-003).
 *
 * 기획서 6. 기술 스택에는 "AI 연동: OpenAI API" 가 명시되어 있고, ERD 문서의 "확인 필요 항목" #2 에는
 *   "AI가 그때그때 생성 vs 미리 만든 문제 은행" 중 어느 쪽인지 박지민(AI/LLM 담당)과 상의가 필요하다고 되어 있다.
 * 그래서 지금은 이 인터페이스만 정의하고, 기본 구현체(MockQuizQuestionGenerator)는 고정된 예시 문제 3개를 돌려준다.
 * 실제 OpenAI 연동이 정해지면 이 인터페이스를 구현하는 OpenAiQuizQuestionGenerator 를 새로 만들어
 * @Primary 로 등록하면 QuizService 코드는 전혀 바꿀 필요가 없다.
 */
public interface QuizQuestionGenerator {

	/**
	 * @param subjectName   오늘 학습한 과목/자격증명 (study_plan.subject_name)
	 * @param todayScope    오늘 완료 처리한 학습 범위(단원/챕터 설명 등). 이 범위 밖의 내용은 출제하지 않는다 (REQ-Q-003).
	 * @return 쉬운 문제(BASIC) 2개 + 응용 문제(APPLIED) 1개, 총 3개 (REQ-Q-002)
	 */
	List<GeneratedQuestion> generate(String subjectName, String todayScope);
}
