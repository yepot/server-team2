package com.hackathon.domain.mypage.service;

import com.hackathon.domain.bookmark.repository.BookmarkRepository;
import com.hackathon.domain.member.entity.Member;
import com.hackathon.domain.member.repository.MemberRepository;
import com.hackathon.domain.mypage.dto.MyPageDto.MyPageResponse;
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
class MyPageServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private BookmarkRepository bookmarkRepository;

	@InjectMocks
	private MyPageService myPageService;

	@Test
	void getMyPageReturnsAggregatedInformation() {
		Member member = createMember(1L, "은서", 720);
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(bookmarkRepository.countByMemberId_IdAndIsActiveTrue(1L)).willReturn(15L);
		given(bookmarkRepository.countCompletedBookmarksByMemberId(1L)).willReturn(7L);
		given(bookmarkRepository.sumViewCountByMemberId(1L)).willReturn(23);

		MyPageResponse response = myPageService.getMyPage(1L);

		assertThat(response.nickname()).isEqualTo("은서");
		assertThat(response.totalScore()).isEqualTo(720);
		assertThat(response.level()).isEqualTo("고등학생");
		assertThat(response.savedBookmarkCount()).isEqualTo(15L);
		assertThat(response.completedBookmarkCount()).isEqualTo(7L);
		assertThat(response.totalViewCount()).isEqualTo(23);
	}

	@Test
	void getMyPageMapsLevelBoundaries() {
		assertThat(getLevelForScore(0)).isEqualTo("유치원생");
		assertThat(getLevelForScore(99)).isEqualTo("유치원생");
		assertThat(getLevelForScore(100)).isEqualTo("초등학생");
		assertThat(getLevelForScore(299)).isEqualTo("초등학생");
		assertThat(getLevelForScore(300)).isEqualTo("중학생");
		assertThat(getLevelForScore(699)).isEqualTo("중학생");
		assertThat(getLevelForScore(700)).isEqualTo("고등학생");
		assertThat(getLevelForScore(1499)).isEqualTo("고등학생");
		assertThat(getLevelForScore(1500)).isEqualTo("대학생");
	}

	@Test
	void getMyPageThrowsWhenMemberDoesNotExist() {
		given(memberRepository.findById(1L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> myPageService.getMyPage(1L))
				.isInstanceOfSatisfying(CustomException.class,
						exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND));
	}

	private String getLevelForScore(int score) {
		Member member = createMember(1L, "은서", score);
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(bookmarkRepository.countByMemberId_IdAndIsActiveTrue(1L)).willReturn(0L);
		given(bookmarkRepository.countCompletedBookmarksByMemberId(1L)).willReturn(0L);
		given(bookmarkRepository.sumViewCountByMemberId(1L)).willReturn(0);

		return myPageService.getMyPage(1L).level();
	}

	private Member createMember(Long memberId, String nickname, int totalScore) {
		Member member = Member.builder()
				.loginId("yepot")
				.password("encoded-password")
				.nickname(nickname)
				.totalScore(totalScore)
				.build();
		ReflectionTestUtils.setField(member, "id", memberId);
		return member;
	}
}
