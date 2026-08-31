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
import com.google.firebase.auth.UserRecord;
import com.planit.global.ApiException;
import com.planit.quiz.QuizService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class AuthController {

	private final QuizService quizService;

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
		String uid = decoded.getUid();
		String email = decoded.getEmail();
		String name = resolveDisplayName(decoded);

		session.setAttribute(SessionUser.UID, uid);
		session.setAttribute(SessionUser.EMAIL, email);
		session.setAttribute(SessionUser.NAME, name);

		log.info("[login] uid={} email={} name={}", uid, email, name);
		return Map.of("uid", uid, "email", nullSafe(email), "name", nullSafe(name));
	}

	/** REQ-A-012: 로그아웃. 세션 무효화. */
	@PostMapping("/logout")
	public Map<String, String> logout(HttpSession session) {
		session.invalidate();
		return Map.of("message", "로그아웃되었습니다");
	}

	/**
	 * 회원 탈퇴. 로그인 세션 필요.
	 *  1) 이 사용자의 Firestore 데이터(퀴즈 기록) 삭제
	 *  2) Firebase Authentication 계정 삭제 (Admin SDK)
	 *  3) 세션 무효화
	 * 되돌릴 수 없다.
	 */
	@PostMapping("/withdraw")
	public Map<String, String> withdraw(HttpSession session) throws Exception {
		requireFirebaseInitialized();
		String uid = SessionUser.uid(session);
		if (uid == null) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다");
		}

		quizService.deleteAllForUser(uid);
		FirebaseAuth.getInstance().deleteUser(uid);
		session.invalidate();

		log.info("[withdraw] uid={} 탈퇴 완료", uid);
		return Map.of("message", "탈퇴가 완료되었습니다");
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

	/**
	 * 표시 이름(회원가입 때 입력한 이름). ID 토큰의 name 클레임이 비어 있으면
	 * (updateProfile 직후 토큰이 아직 갱신 안 된 경우 등) Firebase 사용자 레코드에서 직접 조회한다.
	 */
	private String resolveDisplayName(FirebaseToken decoded) {
		if (decoded.getName() != null && !decoded.getName().isBlank()) {
			return decoded.getName();
		}
		try {
			UserRecord record = FirebaseAuth.getInstance().getUser(decoded.getUid());
			return record.getDisplayName();
		} catch (Exception e) {
			log.warn("[login] displayName 조회 실패 uid={}: {}", decoded.getUid(), e.getMessage());
			return null;
		}
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
