package com.planit.studyplan.dto;

/** REQ-B-002: 목차 파일 업로드 결과. 이 tocFileUrl 을 StudyPlanCreateRequest 에 그대로 담아 보낸다. */
public record TocFileUploadResponse(String tocFileUrl) {
}
