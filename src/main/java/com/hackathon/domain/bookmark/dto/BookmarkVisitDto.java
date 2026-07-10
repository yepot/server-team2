package com.hackathon.domain.bookmark.dto;

import com.hackathon.domain.bookmark.entity.Bookmark;

import java.time.LocalDateTime;

public class BookmarkVisitDto {

	public record Response(
			Long bookmarkId,
			String url,
			Integer viewCount,
			LocalDateTime visitedAt
	) {
		public static Response from(Bookmark bookmark) {
			return new Response(
					bookmark.getId(),
					bookmark.getUrl(),
					bookmark.getViewCount(),
					bookmark.getVisitedAt()
			);
		}
	}
}
