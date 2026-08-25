package com.planit.studyplan.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.planit.global.BaseTimeEntity;
import com.planit.member.entity.Member;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 04_ERD_테이블정의서 "study_plan" 테이블 (김동호 담당 - 학습계획입력, 플랜 생성 마법사 STEP1~2).
 * 관련 화면: PL-001 (planit화면흐름도_수정.pptx)
 *
 * 참고(확인 필요 항목 #1): 과목/단원 우선순위(REQ-B-008)는 "한 플랜에 과목을 여러 개 등록할 수 있는지"부터
 * 팀 결정이 필요해 이번 버전에는 컬럼/기능을 넣지 않았다. 결정되면 이 엔티티에 추가할 것.
 */
@Getter
@Entity
@Table(name = "study_plan")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyPlan extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	/** STEP1: 과목/자격증명 입력 (REQ-B-001) */
	@Column(name = "subject_name", nullable = false, length = 100)
	private String subjectName;

	/** STEP1: 목차 업로드 파일 경로. PDF/JPG/PNG, 최대 10MB (REQ-B-002 / REQ-NF-017) */
	@Column(name = "toc_file_url", length = 500)
	private String tocFileUrl;

	/** STEP2: 학습 시작일 (REQ-B-004) */
	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	/** STEP2: 종료일(시험일 목표일). startDate 보다 빠를 수 없다 (REQ-B-005) */
	@Column(name = "end_date", nullable = false)
	private LocalDate endDate;

	/**
	 * STEP2: 하루 가용 학습 시간(분). (REQ-B-007)
	 * 참고: ERD 작성 시점에 화면에 입력칸이 없어 "분 단위 숫자"로 가정했다. 화면 설계가 확정되면 다시 확인할 것.
	 */
	@Column(name = "daily_available_minutes")
	private Integer dailyAvailableMinutes;

	@OneToMany(mappedBy = "studyPlan", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
	private List<StudyPlanTimeSlot> timeSlots = new ArrayList<>();

	private StudyPlan(Member member, String subjectName, String tocFileUrl,
		LocalDate startDate, LocalDate endDate, Integer dailyAvailableMinutes) {
		this.member = member;
		this.subjectName = subjectName;
		this.tocFileUrl = tocFileUrl;
		this.startDate = startDate;
		this.endDate = endDate;
		this.dailyAvailableMinutes = dailyAvailableMinutes;
	}

	public static StudyPlan create(Member member, String subjectName, String tocFileUrl,
		LocalDate startDate, LocalDate endDate, Integer dailyAvailableMinutes) {
		return new StudyPlan(member, subjectName, tocFileUrl, startDate, endDate, dailyAvailableMinutes);
	}

	/** REQ-B-006: 선호 학습 시간대는 복수 선택 가능 */
	public void addTimeSlot(TimeSlotType timeSlotType) {
		this.timeSlots.add(StudyPlanTimeSlot.of(this, timeSlotType));
	}

	public boolean isOwnedBy(Member other) {
		return this.member.getId().equals(other.getId());
	}
}
