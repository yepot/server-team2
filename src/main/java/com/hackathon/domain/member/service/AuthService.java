package com.hackathon.domain.member.service;

import com.hackathon.domain.member.dto.AuthDto.LoginRequest;
import com.hackathon.domain.member.dto.AuthDto.MemberInfoResponse;
import com.hackathon.domain.member.dto.AuthDto.SignUpRequest;
import com.hackathon.domain.member.dto.AuthDto.TokenResponse;
import com.hackathon.domain.member.entity.Member;
import com.hackathon.domain.member.repository.MemberRepository;
import com.hackathon.global.apiPayload.code.GeneralErrorCode;
import com.hackathon.global.apiPayload.exception.ProjectException;
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
		if (memberRepository.existsByLoginId(request.loginId())) {
			throw new ProjectException(GeneralErrorCode.DUPLICATE_USERNAME);
		}

		Member member = Member.builder()
				.loginId(request.loginId())
				.password(passwordEncoder.encode(request.password()))
				.nickname(request.nickname())
				.totalScore(0)
				.build();

		memberRepository.save(member);
	}

	public TokenResponse login(LoginRequest request) {
		Member member = memberRepository.findByLoginId(request.loginId())
				.orElseThrow(() -> new ProjectException(GeneralErrorCode.MEMBER_NOT_FOUND));

		if (!passwordEncoder.matches(request.password(), member.getPassword())) {
			throw new ProjectException(GeneralErrorCode.INVALID_PASSWORD);
		}

		String accessToken = jwtTokenProvider.createAccessToken(member.getId());
		return new TokenResponse(accessToken);
	}

	public MemberInfoResponse getMyInfo(Long memberId) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new ProjectException(GeneralErrorCode.MEMBER_NOT_FOUND));
		return new MemberInfoResponse(member.getId(), member.getLoginId(), member.getNickname(), member.getTotalScore());
	}

	@Transactional
	public void withdraw(Long memberId) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new ProjectException(GeneralErrorCode.MEMBER_NOT_FOUND));
		memberRepository.delete(member);
	}
}