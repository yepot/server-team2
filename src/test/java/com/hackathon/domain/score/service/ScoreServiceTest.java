package com.hackathon.domain.score.service;

import com.hackathon.domain.member.entity.Member;
import com.hackathon.domain.member.repository.MemberRepository;
import com.hackathon.domain.score.dto.ScoreDto.ScoreResponse;
import com.hackathon.global.exception.CustomException;
import com.hackathon.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ScoreServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private ScoreService scoreService;

	@Test
	void getScoreReturnsMembersTotalScore() {
		Member member = Member.builder()
				.loginId("yepot")
				.password("encoded-password")
				.nickname("은서")
				.totalScore(100)
				.build();
		ReflectionTestUtils.setField(member, "id", 1L);
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));

		ScoreResponse response = scoreService.getScore(1L);

		assertThat(response.totalScore()).isEqualTo("100");
	}

	@Test
	void getScoreThrowsWhenMemberDoesNotExist() {
		given(memberRepository.findById(1L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> scoreService.getScore(1L))
				.isInstanceOfSatisfying(CustomException.class,
						exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND));
	}
}
