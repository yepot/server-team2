package com.hackathon.global.config;

import com.hackathon.global.security.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	private static final String[] PERMIT_ALL = {
			"/api/auth/signup",
			"/api/auth/login",
			"/swagger-ui/**",
			"/v3/api-docs/**",
			"/swagger-ui.html"
	};

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint((request, response, authException) -> {
							String authorization = request.getHeader("Authorization");
							String message = authorization == null || authorization.isBlank()
									? "Access Token은 필수입니다."
									: "유효하지 않은 Access Token입니다.";

							int status = authorization == null || authorization.isBlank()
									? HttpServletResponse.SC_BAD_REQUEST
									: HttpServletResponse.SC_UNAUTHORIZED;
							String error = authorization == null || authorization.isBlank()
									? "Bad Request"
									: "Unauthorized";

							response.setStatus(status);
							response.setContentType(MediaType.APPLICATION_JSON_VALUE);
							response.setCharacterEncoding("UTF-8");
							response.getWriter().write("""
									{"timestamp":"%s","status":%d,"error":"%s","message":"%s","path":"%s"}
									""".formatted(LocalDateTime.now(), status, error, message, request.getRequestURI()));
						})
				)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(PERMIT_ALL).permitAll()
						.anyRequest().authenticated()
				)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOriginPatterns(List.of("*"));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}
