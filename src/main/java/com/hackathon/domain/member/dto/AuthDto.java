package com.hackathon.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDto {

	public record SignUpRequest(
			@NotBlank @Email String email,
			@NotBlank String password,
			@NotBlank String nickname
	) {}

	public record LoginRequest(
			@NotBlank @Email String email,
			@NotBlank String password
	) {}

	public record TokenResponse(
			String accessToken,
			String refreshToken
	) {}
}
