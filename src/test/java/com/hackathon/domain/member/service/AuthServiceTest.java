package com.hackathon.domain.member.service;

import com.hackathon.domain.member.dto.AuthDto.LoginRequest;
import com.hackathon.domain.member.dto.AuthDto.SignUpRequest;
import com.hackathon.domain.member.dto.AuthDto.TokenResponse;
import com.hackathon.domain.bookmark.repository.BookmarkRepository;
import com.hackathon.domain.member.entity.Member;
import com.hackathon.domain.member.repository.MemberRepository;
import com.hackathon.global.exception.CustomException;
import com.hackathon.global.exception.ErrorCode;
import com.hackathon.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private BookmarkRepository bookmarkRepository;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@InjectMocks
	private AuthService authService;

	@Test
	void signUpSavesRequiredMemberFields() {
		SignUpRequest request = new SignUpRequest("yepot", "plain-password", "은서");
		given(memberRepository.existsByLoginId("yepot")).willReturn(false);
		given(passwordEncoder.encode("plain-password")).willReturn("encoded-password");

		authService.signUp(request);

		ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
		verify(memberRepository).save(memberCaptor.capture());

		Member savedMember = memberCaptor.getValue();
		assertThat(savedMember.getLoginId()).isEqualTo("yepot");
		assertThat(savedMember.getPassword()).isEqualTo("encoded-password");
		assertThat(savedMember.getNickname()).isEqualTo("은서");
		assertThat(savedMember.getTotalScore()).isZero();
	}

	@Test
	void loginFindsMemberByLoginId() {
		LoginRequest request = new LoginRequest("yepot", "plain-password");
		Member member = Member.builder()
				.loginId("yepot")
				.password("encoded-password")
				.nickname("은서")
				.totalScore(0)
				.build();
		ReflectionTestUtils.setField(member, "id", 1L);

		given(memberRepository.findByLoginId("yepot")).willReturn(Optional.of(member));
		given(passwordEncoder.matches("plain-password", "encoded-password")).willReturn(true);
		given(jwtTokenProvider.createAccessToken(1L)).willReturn("access-token");
		given(jwtTokenProvider.createRefreshToken(1L)).willReturn("refresh-token");

		TokenResponse response = authService.login(request);

		assertThat(response.accessToken()).isEqualTo("access-token");
		assertThat(response.refreshToken()).isEqualTo("refresh-token");
	}

	@Test
	void signUpThrowsWhenLoginIdAlreadyExists() {
		SignUpRequest request = new SignUpRequest("yepot", "plain-password", "은서");
		given(memberRepository.existsByLoginId("yepot")).willReturn(true);

		assertThatThrownBy(() -> authService.signUp(request))
				.isInstanceOfSatisfying(CustomException.class,
						exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_USERNAME));
	}
}
