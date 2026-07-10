package com.hackathon.domain.mypage.service;

import com.hackathon.domain.bookmark.repository.BookmarkRepository;
import com.hackathon.domain.member.entity.Member;
import com.hackathon.domain.member.repository.MemberRepository;
import com.hackathon.domain.mypage.dto.MyPageDto.MyPageResponse;
import com.hackathon.global.exception.CustomException;
import com.hackathon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

	private final MemberRepository memberRepository;
	private final BookmarkRepository bookmarkRepository;

	public MyPageResponse getMyPage(Long memberId) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		long savedBookmarkCount = bookmarkRepository.countByMemberId_IdAndIsActiveTrue(memberId);
		long completedBookmarkCount = bookmarkRepository.countCompletedBookmarksByMemberId(memberId);
		int totalViewCount = bookmarkRepository.sumViewCountByMemberId(memberId);

		return new MyPageResponse(
				member.getNickname(),
				member.getTotalScore(),
				resolveLevel(member.getTotalScore()),
				savedBookmarkCount,
				completedBookmarkCount,
				totalViewCount
		);
	}

	private String resolveLevel(int totalScore) {
		if (totalScore >= 1500) {
			return "대학생";
		}
		if (totalScore >= 700) {
			return "고등학생";
		}
		if (totalScore >= 300) {
			return "중학생";
		}
		if (totalScore >= 100) {
			return "초등학생";
		}
		return "유치원생";
	}
}
