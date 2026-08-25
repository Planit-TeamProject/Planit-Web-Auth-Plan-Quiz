package com.planit.studyplan.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 목차 파일 저장 방식을 감추는 인터페이스 (REQ-B-002).
 * 지금은 로컬 디스크에 저장하는 LocalFileStorageService 를 쓰지만,
 * 배포 환경이 정해지면(S3 등) 구현체만 새로 만들어 갈아끼우면 된다.
 */
public interface FileStorageService {

	/** 파일을 저장하고, 이후 접근에 사용할 경로(URL)를 반환한다. */
	String store(MultipartFile file);
}
