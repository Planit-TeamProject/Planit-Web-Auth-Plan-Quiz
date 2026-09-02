package com.planit.quiz;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planit.global.ApiException;

import lombok.RequiredArgsConstructor;

/**
 * 개발/테스트용 기본 구현체. 입력과 무관하게 quiz_questions.json 의 예시 문제를 그대로 돌려준다.
 * 예시는 study_plan.json 1일차 범위(운영체제의 개요 / 리눅스의 기초)에 맞췄다.
 * BASIC 2 + APPLIED 1 (REQ-Q-002).
 *
 * OpenAI 연동(박지민 담당)이 정해지면 이 인터페이스를 구현하는 클래스를 @Primary 로 등록하면 된다.
 * 문제 내용을 코드가 아니라 JSON 에 둔 이유: 다른 곳으로 옮기거나 예시를 교체하기 쉽게 하려고.
 */
@Component
@RequiredArgsConstructor
public class MockQuizQuestionGenerator implements QuizQuestionGenerator {

	private static final String QUESTIONS_RESOURCE = "quiz_questions.json";

	private final ObjectMapper objectMapper;

	@Override
	public List<GeneratedQuestion> generate(String subjectName, String todayScope) {
		try (InputStream in = new ClassPathResource(QUESTIONS_RESOURCE).getInputStream()) {
			JsonNode questions = objectMapper.readTree(in).path("questions");
			if (!questions.isArray() || questions.isEmpty()) {
				throw ApiException.notFound("quiz_questions.json 에 questions 가 없습니다");
			}

			List<GeneratedQuestion> result = new ArrayList<>();
			for (JsonNode q : questions) {
				result.add(objectMapper.treeToValue(q, GeneratedQuestion.class));
			}
			return result;
		} catch (IOException e) {
			throw ApiException.notFound("quiz_questions.json 을 읽지 못했습니다");
		}
	}
}
