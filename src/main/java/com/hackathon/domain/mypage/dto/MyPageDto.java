package com.hackathon.domain.mypage.dto;

public class MyPageDto {

	public record MyPageResponse(
			String nickname,
			Integer totalScore,
			String level,
			Long savedBookmarkCount,
			Long completedBookmarkCount,
			Integer totalViewCount
	) {}
}
