package com.planit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Planit 애플리케이션.
 *
 * 데이터/인증은 전부 Firebase(Authentication / Firestore / Storage)로 옮겼고
 * 그 연동은 프론트엔드(frontend/, React)에서 Firebase SDK로 직접 처리한다.
 * 이 Spring 앱은 정적 리소스(src/main/resources/static) 서빙만 담당한다. (MySQL/JPA 제거됨)
 */
@SpringBootApplication
public class PlanitApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanitApplication.class, args);
	}
}
