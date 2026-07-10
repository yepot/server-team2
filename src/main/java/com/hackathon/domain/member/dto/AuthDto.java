package com.hackathon.domain.member.dto;

//import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDto {

	public record SignUpRequest(
			@NotBlank String loginId,
			@NotBlank String password,
			@NotBlank String nickname
	) {}

	public record LoginRequest(
			@NotBlank String loginId,
			@NotBlank String password
	) {}

	public record TokenResponse(
			String accessToken,
			String refreshToken
	) {}
}
