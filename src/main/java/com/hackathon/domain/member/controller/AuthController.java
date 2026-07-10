package com.hackathon.domain.member.controller;

import com.hackathon.domain.member.dto.AuthDto;
import com.hackathon.domain.member.dto.AuthDto.LoginRequest;
import com.hackathon.domain.member.dto.AuthDto.SignUpRequest;
import com.hackathon.domain.member.dto.AuthDto.TokenResponse;
import com.hackathon.domain.member.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/signup")
	@Operation(summary = "회원가입")
	public ResponseEntity<Void> signUp(@Valid @RequestBody SignUpRequest request) {
		authService.signUp(request);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/login")
	@Operation(summary = "로그인", description = "이메일/비밀번호로 로그인 후 access/refresh 토큰 발급")
	public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@GetMapping("/me")
	@Operation(summary = "내 정보 조회")
	public ResponseEntity<AuthDto.MemberInfoResponse> getMyInfo(Authentication authentication) {
		Long memberId = (Long) authentication.getPrincipal();
		return ResponseEntity.ok(authService.getMyInfo(memberId));
	}

//	@PostMapping("/logout")
//	@Operation(summary = "로그아웃")
//	public ResponseEntity<Void> logout() {
//		return ResponseEntity.ok().build();
//	}

	@DeleteMapping("/withdraw")
	@Operation(summary = "회원탈퇴")
	public ResponseEntity<Void> withdraw(Authentication authentication) {
		Long memberId = (Long) authentication.getPrincipal();
		authService.withdraw(memberId);
		return ResponseEntity.ok().build();
	}
}
