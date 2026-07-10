package com.hackathon.domain.bookmark.dto;

import com.hackathon.domain.bookmark.entity.Bookmark;
import com.hackathon.domain.bookmark.entity.BookmarkTag;
import com.hackathon.domain.checklist.entity.Checklist;

import java.util.List;
import java.util.Map;

public class BookmarkReadDto {

	public record Response(
			List<BookmarkResponse> bookmarks
	) {
		public static Response of(List<BookmarkResponse> bookmarks) {
			return new Response(bookmarks);
		}
	}

	public record BookmarkResponse(
			Long bookmarkId,
			String title,
			String url,
			List<TagResponse> tags,
			List<ChecklistResponse> checklists
	) {
		public static BookmarkResponse of(Bookmark bookmark, Map<Long, List<Checklist>> checklistsByBookmarkId) {
			return new BookmarkResponse(
					bookmark.getId(),
					bookmark.getTitle(),
					bookmark.getUrl(),
					bookmark.getTags().stream()
							.map(TagResponse::from)
							.toList(),
					checklistsByBookmarkId.getOrDefault(bookmark.getId(), List.of()).stream()
							.map(ChecklistResponse::from)
							.toList()
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

	public record ChecklistResponse(
			Long checklistId,
			String content,
			Boolean isChecked
	) {
		public static ChecklistResponse from(Checklist checklist) {
			return new ChecklistResponse(
					checklist.getId(),
					checklist.getContent(),
					checklist.isChecked()
			);
		}
	}

	public record DetailResponse(
			Long bookmarkId,
			String title,
			String url,
			java.time.LocalDateTime remindAt,
			Integer totalScore,
			List<TagResponse> tags,
			List<ChecklistResponse> checklists
	) {
		public static DetailResponse of(Bookmark bookmark, List<Checklist> checklists) {
			return new DetailResponse(
					bookmark.getId(),
					bookmark.getTitle(),
					bookmark.getUrl(),
					bookmark.getRemindAt(),
					bookmark.getMemberId().getTotalScore(),
					bookmark.getTags().stream()
							.map(TagResponse::from)
							.toList(),
					checklists.stream()
							.map(ChecklistResponse::from)
							.toList()
			);
		}
	}

	public record TagFilterResponse(
			String tagName,
			List<BookmarkResponse> bookmarks
	) {
		public static TagFilterResponse of(String tagName, List<BookmarkResponse> bookmarks) {
			return new TagFilterResponse(tagName, bookmarks);
		}
	}
}
