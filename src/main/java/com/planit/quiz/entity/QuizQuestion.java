package com.planit.quiz.entity;

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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 04_ERD_테이블정의서 "quiz_question" 테이블.
 * 퀴즈 세트의 문제(4지선다)와 정답/풀이 (REQ-Q-002, REQ-Q-003, REQ-Q-005).
 *
 * 참고(확인 필요 항목 #2): AI가 그때그때 문제를 생성한다고 가정하고 만든 구조.
 * 미리 만든 문제 은행 방식으로 바뀌면 구조 변경이 필요할 수 있다 -> QuizQuestionGenerator 참고.
 */
@Getter
@Entity
@Table(name = "quiz_question")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizQuestion extends BaseCreatedAtEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "quiz_id", nullable = false)
	private Quiz quiz;

	/** 1~3번 (REQ-Q-002) */
	@Column(name = "question_no", nullable = false)
	private int questionNo;

	@Enumerated(EnumType.STRING)
	@Column(name = "question_type", nullable = false, length = 10)
	private QuestionType questionType;

	// length 를 지정하지 않으면 Hibernate 가 기본값(255)을 기준으로 TINYTEXT 를 기대해서
	// docs/schema.sql 의 TEXT 컬럼과 검증(ddl-auto=validate)이 어긋난다. 65535 로 맞춰 TEXT 로 매핑되게 한다.
	@Lob
	@Column(name = "question_text", nullable = false, length = 65535)
	private String questionText;

	@Column(nullable = false, length = 255)
	private String choice1;

	@Column(nullable = false, length = 255)
	private String choice2;

	@Column(nullable = false, length = 255)
	private String choice3;

	@Column(nullable = false, length = 255)
	private String choice4;

	/** 1~4 중 정답 보기 번호 */
	@Column(name = "answer_no", nullable = false)
	private int answerNo;

	/** 제출 후 문제 아래에 보여줄 풀이 (REQ-Q-005) */
	@Lob
	@Column(nullable = false, length = 65535)
	private String explanation;

	public QuizQuestion(Quiz quiz, int questionNo, QuestionType questionType, String questionText,
		String choice1, String choice2, String choice3, String choice4, int answerNo, String explanation) {
		this.quiz = quiz;
		this.questionNo = questionNo;
		this.questionType = questionType;
		this.questionText = questionText;
		this.choice1 = choice1;
		this.choice2 = choice2;
		this.choice3 = choice3;
		this.choice4 = choice4;
		this.answerNo = answerNo;
		this.explanation = explanation;
	}

	public boolean isCorrect(int selectedChoice) {
		return this.answerNo == selectedChoice;
	}
}
