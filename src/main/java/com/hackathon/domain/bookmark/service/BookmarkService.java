package com.hackathon.domain.bookmark.service;

import com.hackathon.domain.bookmark.dto.BookmarkCreateDto.Request;
import com.hackathon.domain.bookmark.dto.BookmarkCreateDto.Response;
import com.hackathon.domain.bookmark.dto.BookmarkDeleteDto;
import com.hackathon.domain.bookmark.dto.BookmarkReadDto;
import com.hackathon.domain.bookmark.dto.BookmarkUpdateDto;
import com.hackathon.domain.bookmark.dto.BookmarkVisitDto;
import com.hackathon.domain.bookmark.entity.Bookmark;
import com.hackathon.domain.bookmark.repository.BookmarkRepository;
import com.hackathon.domain.checklist.dto.ChecklistDto.CreateRequest;
import com.hackathon.domain.checklist.entity.Checklist;
import com.hackathon.domain.checklist.repository.ChecklistRepository;
import com.hackathon.domain.checklist.service.ChecklistService;
import com.hackathon.domain.member.entity.Member;
import com.hackathon.domain.member.repository.MemberRepository;
import com.hackathon.domain.score.service.ScoreService;
import com.hackathon.global.exception.CustomException;
import com.hackathon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

	private static final int MAX_TAG_COUNT = 5;
	private static final int MAX_CHECKLIST_COUNT = 5;
	private static final String DEFAULT_CHECKLIST = "링크 열람하기";

	private final BookmarkRepository bookmarkRepository;
	private final ChecklistRepository checklistRepository;
	private final ChecklistService checklistService;
	private final MemberRepository memberRepository;
	private final ScoreService scoreService;

	@Transactional
	public Response create(Long memberId, Request request) {
		validateAuthenticatedMember(memberId);
		validateRequest(request);

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		Bookmark bookmark = Bookmark.builder()
				.memberId(member)
				.title(request.title())
				.url(request.url())
				.remindAt(request.remindAt())
				.build();

		if (request.tags() != null) {
			bookmark.replaceTags(request.tags());
		}

		Bookmark savedBookmark = bookmarkRepository.saveAndFlush(bookmark);
		awardCreateScores(savedBookmark, request);
		createInitialChecklists(memberId, savedBookmark.getId(), request.checklists());

		return Response.from(savedBookmark);
	}

	public BookmarkReadDto.Response findAll(Long memberId) {
		validateAuthenticatedMember(memberId);
		List<Bookmark> bookmarks = bookmarkRepository.findOwnedActiveBookmarks(memberId);
		Map<Long, List<Checklist>> checklistsByBookmarkId = bookmarks.stream()
				.collect(Collectors.toMap(
						Bookmark::getId,
						bookmark -> checklistRepository.findByBookmarkId(bookmark.getId())
				));

		return BookmarkReadDto.Response.of(bookmarks.stream()
				.map(bookmark -> BookmarkReadDto.BookmarkResponse.of(bookmark, checklistsByBookmarkId))
				.toList());
	}

	public BookmarkReadDto.DetailResponse findOne(Long memberId, Long bookmarkId) {
		validateAuthenticatedMember(memberId);
		Bookmark bookmark = bookmarkRepository.findActiveBookmarkWithTags(bookmarkId)
				.orElseThrow(() -> new CustomException(ErrorCode.BOOKMARK_NOT_FOUND));

		if (!bookmark.getMemberId().getId().equals(memberId)) {
			throw new CustomException(ErrorCode.FORBIDDEN_BOOKMARK_ACCESS);
		}

		List<Checklist> checklists = checklistRepository.findByBookmarkId(bookmarkId);
		return BookmarkReadDto.DetailResponse.of(bookmark, checklists);
	}

	@Transactional
	public BookmarkUpdateDto.Response update(Long memberId, Long bookmarkId, BookmarkUpdateDto.Request request) {
		validateAuthenticatedMember(memberId);
		validateUpdateRequest(request);

		Bookmark bookmark = bookmarkRepository.findActiveBookmarkWithTags(bookmarkId)
				.orElseThrow(() -> new CustomException(ErrorCode.BOOKMARK_NOT_FOUND));

		if (!bookmark.getMemberId().getId().equals(memberId)) {
			throw new CustomException(ErrorCode.FORBIDDEN_BOOKMARK_UPDATE);
		}

		bookmark.update(request.url(), request.title(), null, request.remindAt());
		if (request.tags() != null) {
			bookmark.replaceTags(request.tags());
		}

		bookmarkRepository.flush();
		return BookmarkUpdateDto.Response.from(bookmark);
	}

	@Transactional
	public BookmarkVisitDto.Response visit(Long memberId, Long bookmarkId) {
		validateAuthenticatedMember(memberId);
		Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
				.orElseThrow(() -> new CustomException(ErrorCode.BOOKMARK_NOT_FOUND));

		if (!bookmark.getIsActive()) {
			throw new CustomException(ErrorCode.BOOKMARK_UNAVAILABLE);
		}
		if (!bookmark.getMemberId().getId().equals(memberId)) {
			throw new CustomException(ErrorCode.FORBIDDEN_BOOKMARK_ACCESS);
		}

		bookmark.visit();
		scoreService.awardReminderRevisit(bookmark);
		bookmarkRepository.flush();
		return BookmarkVisitDto.Response.from(bookmark);
	}

	@Transactional
	public BookmarkDeleteDto.Response delete(Long memberId, Long bookmarkId) {
		validateAuthenticatedMember(memberId);
		Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
				.orElseThrow(() -> new CustomException(ErrorCode.BOOKMARK_NOT_FOUND));

		if (!bookmark.getIsActive()) {
			throw new CustomException(ErrorCode.BOOKMARK_ALREADY_DELETED);
		}
		if (!bookmark.getMemberId().getId().equals(memberId)) {
			throw new CustomException(ErrorCode.FORBIDDEN_BOOKMARK_DELETE);
		}

		bookmark.delete();
		bookmarkRepository.flush();
		return BookmarkDeleteDto.Response.of(bookmark.getId());
	}

	private void validateAuthenticatedMember(Long memberId) {
		if (memberId == null) {
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
	}

	private void validateRequest(Request request) {
		if (request.tags() != null && request.tags().size() > MAX_TAG_COUNT) {
			throw new IllegalArgumentException("해시태그는 최대 5개까지 등록할 수 있습니다.");
		}
		if (request.checklists() != null && request.checklists().size() > MAX_CHECKLIST_COUNT) {
			throw new IllegalArgumentException("체크리스트는 최대 5개까지 등록할 수 있습니다.");
		}
		if (request.remindAt() != null && !request.remindAt().isAfter(LocalDateTime.now())) {
			throw new IllegalArgumentException("리마인드 날짜는 현재 시각 이후로 설정해야 합니다.");
		}
		if (!isValidUrl(request.url())) {
			throw new IllegalArgumentException("올바른 URL 형식이 아닙니다.");
		}
	}

	private void createInitialChecklists(Long memberId, Long bookmarkId, List<String> checklists) {
		List<String> checklistContents = checklists == null || checklists.isEmpty()
				? List.of(DEFAULT_CHECKLIST)
				: checklists;

		checklistContents.forEach(content ->
				checklistService.createChecklist(memberId, bookmarkId, new CreateRequest(content))
		);
	}

	private void awardCreateScores(Bookmark bookmark, Request request) {
		scoreService.awardLinkSaved(bookmark);

		if (request.title() != null && !request.title().isBlank()) {
			scoreService.awardPurposeSet(bookmark);
		}
		if (request.tags() != null && !request.tags().isEmpty()) {
			scoreService.awardTagSet(bookmark);
		}
		if (request.remindAt() != null) {
			scoreService.awardReminderSet(bookmark);
		}
	}

	private void validateUpdateRequest(BookmarkUpdateDto.Request request) {
		if (request.url() == null && request.title() == null && request.tags() == null && request.remindAt() == null) {
			throw new IllegalArgumentException("수정할 북마크 정보를 하나 이상 입력해야 합니다.");
		}
		if (request.tags() != null && request.tags().size() > MAX_TAG_COUNT) {
			throw new IllegalArgumentException("해시태그는 최대 5개까지 등록할 수 있습니다.");
		}
		if (request.remindAt() != null && !request.remindAt().isAfter(LocalDateTime.now())) {
			throw new IllegalArgumentException("리마인드 날짜는 현재 시각 이후로 설정해야 합니다.");
		}
		if (request.url() != null && !isValidUrl(request.url())) {
			throw new IllegalArgumentException("올바른 URL 형식이 아닙니다.");
		}
	}

	private boolean isValidUrl(String url) {
		if (url == null || url.isBlank()) {
			return false;
		}
		try {
			URI uri = URI.create(url);
			return uri.getScheme() != null
					&& uri.getHost() != null
					&& ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()));
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	public BookmarkReadDto.TagFilterResponse findAllByTag(Long memberId, String tagName) {
		validateAuthenticatedMember(memberId);

		List<Bookmark> bookmarks = bookmarkRepository.findOwnedActiveBookmarksByTagName(memberId, tagName);
		List<Long> bookmarkIds = bookmarks.stream()
				.map(Bookmark::getId)
				.toList();
		Map<Long, List<Checklist>> checklistsByBookmarkId = bookmarkIds.isEmpty()
				? Map.of()
				: checklistRepository.findByBookmark_IdInOrderByIdAsc(bookmarkIds).stream()
				.collect(Collectors.groupingBy(checklist -> checklist.getBookmark().getId()));

		List<BookmarkReadDto.BookmarkResponse> responses = bookmarks.stream()
				.map(bookmark -> BookmarkReadDto.BookmarkResponse.of(bookmark, checklistsByBookmarkId))
				.toList();

		return BookmarkReadDto.TagFilterResponse.of(tagName, responses);
	}
}
