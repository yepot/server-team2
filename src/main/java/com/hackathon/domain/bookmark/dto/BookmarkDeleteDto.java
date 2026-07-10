package com.hackathon.domain.bookmark.dto;

public class BookmarkDeleteDto {

	public record Response(
			Long bookmarkId,
			String message
	) {
		public static Response of(Long bookmarkId) {
			return new Response(bookmarkId, "북마크가 성공적으로 삭제되었습니다.");
		}
	}
}
