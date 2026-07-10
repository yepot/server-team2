package com.hackathon.domain.member.service;

import com.hackathon.domain.member.dto.AuthDto.LoginRequest;
import com.hackathon.domain.member.dto.AuthDto.SignUpRequest;
import com.hackathon.domain.member.dto.AuthDto.TokenResponse;
import com.hackathon.domain.member.entity.Member;
import com.hackathon.domain.member.repository.MemberRepository;
import com.hackathon.global.exception.CustomException;
import com.hackathon.global.exception.ErrorCode;
import com.hackathon.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	@Transactional
	public void signUp(SignUpRequest request) {
		if (memberRepository.existsByUsername(request.loginId())) {
			throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
		}

		Member member = Member.builder()
				.username(request.loginId())
				.password(passwordEncoder.encode(request.password()))
				.nickname(request.nickname())
				.build();

		memberRepository.save(member);
	}

	public TokenResponse login(LoginRequest request) {
		Member member = memberRepository.findByUsername(request.loginId())
				.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		if (!passwordEncoder.matches(request.password(), member.getPassword())) {
			throw new CustomException(ErrorCode.INVALID_PASSWORD);
		}

		String accessToken = jwtTokenProvider.createAccessToken(member.getId());
		String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());

		return new TokenResponse(accessToken, refreshToken);
	}
}
