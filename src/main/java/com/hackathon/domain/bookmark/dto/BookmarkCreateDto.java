package com.hackathon.domain.bookmark.dto;

import com.hackathon.domain.bookmark.entity.Bookmark;
import com.hackathon.domain.bookmark.entity.BookmarkTag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class BookmarkCreateDto {

	public record Request(
			@NotBlank(message = "제목은 필수입니다.")
			@Size(max = 500)
			String title,

			@NotBlank(message = "올바른 URL 형식이 아닙니다.")
			@Size(max = 2048)
			String url,

			LocalDateTime remindAt,

			List<@NotBlank @Size(max = 50) String> tags
	) {
	}

	public record Response(
			Long bookmarkId,
			String title,
			String url,
			Integer viewCount,
			LocalDateTime visitedAt,
			LocalDateTime remindAt,
			Boolean isActive,
			List<TagResponse> tags,
			LocalDateTime createdAt,
			LocalDateTime updatedAt
	) {
		public static Response from(Bookmark bookmark) {
			return new Response(
					bookmark.getId(),
					bookmark.getTitle(),
					bookmark.getUrl(),
					bookmark.getViewCount(),
					bookmark.getVisitedAt(),
					bookmark.getRemindAt(),
					bookmark.getIsActive(),
					bookmark.getTags().stream()
							.map(TagResponse::from)
							.toList(),
					bookmark.getCreatedAt(),
					bookmark.getUpdatedAt()
			);
		}
	}

	public record TagResponse(
			Long tagId,
			String name
	) {
		public static TagResponse from(BookmarkTag tag) {
			return new TagResponse(tag.getId(), tag.getName());
		}
	}
}
