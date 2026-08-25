package com.planit.quiz.entity;

import java.time.LocalDateTime;

import com.planit.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 04_ERD_테이블정의서 "quiz_answer" 테이블.
 * 회원이 퀴즈 문제에 제출한 답과 정답 여부. 응시 기록은 계속 보관한다 (REQ-Q-004, REQ-Q-005 / REQ-NF-023).
 */
@Getter
@Entity
@Table(
	name = "quiz_answer",
	// "문제 하나당 1번만 제출" (ERD 비고, REQ-NF-015) -> DB 유니크 제약으로 강제
	uniqueConstraints = @UniqueConstraint(columnNames = {"quiz_question_id", "member_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizAnswer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "quiz_question_id", nullable = false)
	private QuizQuestion quizQuestion;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	/** 1~4 중 고른 보기 (REQ-Q-004) */
	@Column(name = "selected_choice", nullable = false)
	private int selectedChoice;

	/** 제출한 순간 채점해서 저장 (REQ-Q-005) */
	@Column(name = "is_correct", nullable = false)
	private boolean correct;

	@Column(name = "answered_at", nullable = false)
	private LocalDateTime answeredAt;

	private QuizAnswer(QuizQuestion quizQuestion, Member member, int selectedChoice, boolean correct) {
		this.quizQuestion = quizQuestion;
		this.member = member;
		this.selectedChoice = selectedChoice;
		this.correct = correct;
		this.answeredAt = LocalDateTime.now();
	}

	public static QuizAnswer submit(QuizQuestion quizQuestion, Member member, int selectedChoice) {
		boolean correct = quizQuestion.isCorrect(selectedChoice);
		return new QuizAnswer(quizQuestion, member, selectedChoice, correct);
	}
}
