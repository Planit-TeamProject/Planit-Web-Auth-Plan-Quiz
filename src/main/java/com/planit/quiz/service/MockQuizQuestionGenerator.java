package com.planit.quiz.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.planit.quiz.entity.QuestionType;

/**
 * 개발/테스트 단계용 기본 구현체.
 * 실제 OpenAI 연동 전까지, 항상 고정된 3문제(BASIC 2개 + APPLIED 1개)를 돌려준다.
 * 문제 내용에 subjectName 을 넣어, 최소한 "어느 과목 문제인지"는 알아볼 수 있게 했다.
 */
@Component
public class MockQuizQuestionGenerator implements QuizQuestionGenerator {

	@Override
	public List<GeneratedQuestion> generate(String subjectName, String todayScope) {
		return List.of(
			new GeneratedQuestion(
				QuestionType.BASIC,
				"[임시 문제] '%s' 오늘 학습 범위(%s)에서 배운 핵심 용어는 무엇일까요?".formatted(subjectName, todayScope),
				"보기 1", "보기 2", "보기 3", "보기 4",
				1,
				"[임시 풀이] 실제 서비스에서는 OpenAI API 연동 후 이 자리에 진짜 문제/풀이가 채워집니다."
			),
			new GeneratedQuestion(
				QuestionType.BASIC,
				"[임시 문제 2] '%s' 오늘 학습 범위(%s)의 두 번째 핵심 개념은?".formatted(subjectName, todayScope),
				"보기 1", "보기 2", "보기 3", "보기 4",
				2,
				"[임시 풀이] 실제 서비스에서는 OpenAI API 연동 후 이 자리에 진짜 문제/풀이가 채워집니다."
			),
			new GeneratedQuestion(
				QuestionType.APPLIED,
				"[임시 응용 문제] '%s' 오늘 학습 범위(%s)를 실제 상황에 적용한다면?".formatted(subjectName, todayScope),
				"보기 1", "보기 2", "보기 3", "보기 4",
				3,
				"[임시 풀이] 실제 서비스에서는 OpenAI API 연동 후 이 자리에 진짜 문제/풀이가 채워집니다."
			)
		);
	}
}
