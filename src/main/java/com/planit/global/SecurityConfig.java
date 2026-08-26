package com.planit.global;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planit.global.exception.ErrorCode;
import com.planit.global.exception.ErrorResponse;
import com.planit.member.service.MemberDetailsService;

/**
 * 기획서 6. 기술 스택 - "Spring Security, 로그인 처리 표준 구현".
 *
 * 로그인은 AuthController 가 받아서 AuthService 를 통해 AuthenticationManager 로 인증하는 표준 흐름을 따른다.
 * AuthService 가 Security 의 예외(LockedException/DisabledException/BadCredentialsException)를 잡아
 * 요구사항정의서 문구 그대로의 BusinessException 으로 바꿔주고, 세션 저장까지 책임진다.
 * 이 클래스는 그 외 API 에 대한 인가(로그인 여부 확인)와 CORS/CSRF 등 전역 보안 설정만 담당한다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	/** REQ-NF-009: 비밀번호는 BCrypt 로 암호화해서 저장한다. */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * 로그인(REQ-A-008 ~ REQ-A-011, REQ-A-015)에 사용하는 표준 Spring Security 인증 흐름.
	 * MemberDetailsService(email 로 회원 조회) + PasswordEncoder(BCrypt 비교) 조합으로 인증하며,
	 * MemberDetails 의 isAccountNonLocked()/isEnabled() 값에 따라 LockedException/DisabledException 을
	 * 자동으로 던져준다 (AuthService#login 에서 각각 REQ-NF-013 / REQ-A-015 메시지로 변환).
	 */
	@Bean
	public DaoAuthenticationProvider daoAuthenticationProvider(
		MemberDetailsService memberDetailsService, PasswordEncoder passwordEncoder
	) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(memberDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return provider;
	}

	@Bean
	public AuthenticationManager authenticationManager(DaoAuthenticationProvider daoAuthenticationProvider) {
		return new ProviderManager(daoAuthenticationProvider);
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
		http
			// React(SPA) 에서 세션 쿠키로 호출하는 REST API 이므로 폼 기반 CSRF 는 사용하지 않는다.
			// (운영 배포 전, 팀 논의를 통해 CSRF 방어 방식을 다시 확인할 것)
			.csrf(csrf -> csrf.disable())
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
			// 이 컨트롤러들은 AuthController 가 직접 처리하므로 Security 기본 필터는 끈다.
			.formLogin(form -> form.disable())
			.httpBasic(basic -> basic.disable())
			.logout(logout -> logout.disable())
			.authorizeHttpRequests(auth -> auth
				// 목업 화면(정적 리소스, planit-mockup.html 기반). 로그인 화면 자체는 로그인 없이도 볼 수 있어야 한다.
				.requestMatchers(
					"/", "/index.html", "/favicon.ico",
					"/*.css", "/*.js", "/*.ico", "/*.png", "/*.svg", "/*.json"
				).permitAll()
				.requestMatchers(
					"/api/auth/signup",
					"/api/auth/login",
					"/api/auth/email-verification",
					"/api/auth/email-verification/resend"
				).permitAll()
				// [테스트용] 학습 계획/로그인 없이 퀴즈 문제 생성만 확인하는 임시 엔드포인트. 배포 전 제거할 것.
				.requestMatchers("/api/quizzes/preview").permitAll()
				.anyRequest().authenticated()
			)
			// 로그인 안 된 상태로 보호된 API 를 호출하면 로그인 화면으로 리다이렉트하지 않고,
			// GlobalExceptionHandler 가 내려주는 것과 같은 모양의 401 JSON 을 내려준다.
			.exceptionHandling(handling -> handling
				.authenticationEntryPoint((request, response, authException) -> {
					ErrorCode errorCode = ErrorCode.LOGIN_REQUIRED;
					response.setStatus(errorCode.getStatus().value());
					response.setContentType(MediaType.APPLICATION_JSON_VALUE);
					response.setCharacterEncoding("UTF-8");
					ErrorResponse body = ErrorResponse.builder()
						.code(errorCode.name())
						.message(errorCode.getMessage())
						.build();
					objectMapper.writeValue(response.getWriter(), body);
				})
			);

		return http.build();
	}

	/**
	 * React 개발 서버(예: http://localhost:3000)에서 세션 쿠키를 주고받으려면
	 * allowCredentials(true) + 명시적 origin 이 필요하다 (allowedOrigins("*") 와 함께 쓸 수 없음).
	 * 실제 배포 도메인이 정해지면 allowedOrigins 값을 팀과 함께 갱신할 것.
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of("http://localhost:3000"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
