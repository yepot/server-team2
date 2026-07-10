package com.hackathon.domain.bookmark.dto;

import com.hackathon.domain.bookmark.entity.Bookmark;
import com.hackathon.domain.bookmark.entity.BookmarkTag;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class BookmarkUpdateDto {

	public record Request(
			@Size(max = 2048)
			String url,

			@Size(max = 500)
			String title,

			List<@Size(max = 50) String> tags,

			LocalDateTime remindAt
	) {
	}

	public record Response(
			Long bookmarkId,
			String url,
			String title,
			List<TagResponse> tags,
			LocalDateTime remindAt,
			LocalDateTime updatedAt
	) {
		public static Response from(Bookmark bookmark) {
			return new Response(
					bookmark.getId(),
					bookmark.getUrl(),
					bookmark.getTitle(),
					bookmark.getTags().stream()
							.map(TagResponse::from)
							.toList(),
					bookmark.getRemindAt(),
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
