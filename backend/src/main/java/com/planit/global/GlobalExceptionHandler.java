package com.planit.global;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.google.firebase.auth.FirebaseAuthException;

import lombok.extern.slf4j.Slf4j;

/** REST 컨트롤러 예외를 { "message": ... } JSON 으로 통일한다. */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<Map<String, String>> handleApi(ApiException e) {
		return ResponseEntity.status(e.getStatus()).body(Map.of("message", e.getMessage()));
	}

	@ExceptionHandler(FirebaseAuthException.class)
	public ResponseEntity<Map<String, String>> handleFirebaseAuth(FirebaseAuthException e) {
		log.warn("[FirebaseAuthException] {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
			.body(Map.of("message", "로그인 인증에 실패했습니다"));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handleEtc(Exception e) {
		log.error("[Unhandled] {}", e.getMessage(), e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(Map.of("message", "서버 오류가 발생했습니다"));
	}
}
