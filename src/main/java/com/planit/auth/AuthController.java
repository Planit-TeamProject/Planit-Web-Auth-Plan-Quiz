package com.planit.auth;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.planit.global.ApiException;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * 방식 B 인증 흐름.
 *  1) 브라우저: Firebase JS 로 이메일/비밀번호(또는 구글) 로그인 → user.getIdToken()
 *  2) 브라우저 → POST /api/auth/firebase-login { idToken }
 *  3) 여기서 Firebase Admin SDK 로 토큰 검증 → 세션에 uid/email 저장
 *  4) 이후 모든 데이터 API 는 세션 쿠키만으로 인증 (브라우저는 Firestore 를 직접 건드리지 않음)
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	/** REQ-A-008 ~ REQ-A-011: 로그인. body: { "idToken": "..." } */
	@PostMapping("/firebase-login")
	public Map<String, String> firebaseLogin(@RequestBody Map<String, String> body, HttpSession session)
		throws FirebaseAuthException {
		requireFirebaseInitialized();

		String idToken = body.get("idToken");
		if (idToken == null || idToken.isBlank()) {
			throw ApiException.badRequest("idToken 이 없습니다");
		}

		FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);
		session.setAttribute(SessionUser.UID, decoded.getUid());
		session.setAttribute(SessionUser.EMAIL, decoded.getEmail());
		session.setAttribute(SessionUser.NAME, decoded.getName());

		log.info("[login] uid={} email={} name={}", decoded.getUid(), decoded.getEmail(), decoded.getName());
		return Map.of(
			"uid", decoded.getUid(),
			"email", nullSafe(decoded.getEmail()),
			"name", nullSafe(decoded.getName())
		);
	}

	/** REQ-A-012: 로그아웃. 세션 무효화. */
	@PostMapping("/logout")
	public Map<String, String> logout(HttpSession session) {
		session.invalidate();
		return Map.of("message", "로그아웃되었습니다");
	}

	/** 현재 로그인 상태 확인. 로그인 안 되어 있으면 401. */
	@GetMapping("/me")
	public Map<String, String> me(HttpSession session) {
		String uid = SessionUser.uid(session);
		if (uid == null) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다");
		}
		return Map.of(
			"uid", uid,
			"email", nullSafe(SessionUser.email(session)),
			"name", nullSafe(SessionUser.name(session))
		);
	}

	private void requireFirebaseInitialized() {
		if (FirebaseApp.getApps().isEmpty()) {
			throw ApiException.serviceUnavailable(
				"서버에 Firebase 서비스 계정 키가 설정되지 않았습니다. "
					+ "src/main/resources/firebase-service-account.json 을 두고 서버를 재시작하세요.");
		}
	}

	private String nullSafe(String v) {
		return v == null ? "" : v;
	}
}
