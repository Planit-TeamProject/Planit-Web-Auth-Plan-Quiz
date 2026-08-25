package com.planit.quiz.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.planit.global.BaseCreatedAtEntity;
import com.planit.member.entity.Member;
import com.planit.studyplan.entity.StudyPlan;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 04_ERD_테이블정의서 "quiz" 테이블 (김동호 담당 - 퀴즈봇).
 * 하루 학습 체크리스트를 100% 완료하면 그날 학습 범위로 만들어지는 하루 단위 퀴즈. (REQ-Q-001)
 * 관련 화면: QZ-001 (planit화면흐름도_수정.pptx)
 *
 * 참고(확인 필요 항목 #3): 퀴즈 결과가 체크리스트 완료 상태나 계획 재조정에 영향을 주는지는
 * 아직 팀 결정 전이라, 이 엔티티는 그 연결(FK)을 갖지 않는다.
 */
@Getter
@Entity
@Table(
	name = "quiz",
	// "회원+학습계획+날짜 조합으로 하루 1세트만" (ERD 비고) -> DB 유니크 제약으로 강제
	uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "study_plan_id", "quiz_date"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz extends BaseCreatedAtEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "study_plan_id", nullable = false)
	private StudyPlan studyPlan;

	/** 체크리스트를 100% 완료한 날짜 (REQ-Q-001, REQ-Q-003) */
	@Column(name = "quiz_date", nullable = false)
	private LocalDate quizDate;

	@OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<QuizQuestion> questions = new ArrayList<>();

	private Quiz(Member member, StudyPlan studyPlan, LocalDate quizDate) {
		this.member = member;
		this.studyPlan = studyPlan;
		this.quizDate = quizDate;
	}

	public static Quiz create(Member member, StudyPlan studyPlan, LocalDate quizDate) {
		return new Quiz(member, studyPlan, quizDate);
	}

	public void addQuestion(QuizQuestion question) {
		this.questions.add(question);
	}

	public boolean isOwnedBy(Member other) {
		return this.member.getId().equals(other.getId());
	}
}
