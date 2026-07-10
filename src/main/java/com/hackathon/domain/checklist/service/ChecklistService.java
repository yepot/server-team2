package com.hackathon.domain.checklist.service;

import com.hackathon.domain.bookmark.entity.Bookmark;
import com.hackathon.domain.bookmark.repository.BookmarkRepository;
import com.hackathon.domain.checklist.dto.ChecklistDto.ChecklistCheckResponse;
import com.hackathon.domain.checklist.dto.ChecklistDto.ChecklistResponse;
import com.hackathon.domain.checklist.dto.ChecklistDto.CreateRequest;
import com.hackathon.domain.checklist.dto.ChecklistDto.UpdateRequest;
import com.hackathon.domain.checklist.entity.Checklist;
import com.hackathon.domain.checklist.repository.ChecklistRepository;
import com.hackathon.domain.score.service.ScoreService;
import com.hackathon.global.exception.CustomException;
import com.hackathon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChecklistService {

	private final ChecklistRepository checklistRepository;
	private final BookmarkRepository bookmarkRepository;
	private final ScoreService scoreService;

	@Transactional
	public ChecklistResponse createChecklist(Long memberId, Long bookmarkId, CreateRequest request) {
		Bookmark bookmark = getOwnedBookmark(bookmarkId, memberId);
		Checklist checklist = checklistRepository.saveAndFlush(new Checklist(bookmark, request.content()));

		return ChecklistResponse.from(checklist);
	}

	@Transactional
	public ChecklistResponse updateChecklist(Long memberId, Long bookmarkId, Long checklistId, UpdateRequest request) {
		Checklist checklist = getOwnedChecklist(checklistId, bookmarkId, memberId);
		checklist.updateContent(request.content());

		return ChecklistResponse.from(checklist);
	}

	@Transactional
	public void deleteChecklist(Long memberId, Long bookmarkId, Long checklistId) {
		Checklist checklist = getOwnedChecklist(checklistId, bookmarkId, memberId);
		checklistRepository.delete(checklist);
	}

	@Transactional
	public ChecklistCheckResponse toggleChecklist(Long memberId, Long bookmarkId, Long checklistId) {
		Checklist checklist = getOwnedChecklist(checklistId, bookmarkId, memberId);
		boolean wasChecked = checklist.isChecked();
		checklist.toggleChecked();
		if (!wasChecked && checklist.isChecked()) {
			scoreService.awardChecklistChecked(checklist);
		}
		checklistRepository.flush();

		return ChecklistCheckResponse.from(checklist);
	}

	private Bookmark getOwnedBookmark(Long bookmarkId, Long memberId) {
		return bookmarkRepository.findOwnedActiveBookmark(bookmarkId, memberId)
				.orElseThrow(() -> new CustomException(ErrorCode.BOOKMARK_NOT_FOUND));
	}

	private Checklist getOwnedChecklist(Long checklistId, Long bookmarkId, Long memberId) {
		return checklistRepository.findOwnedChecklist(checklistId, bookmarkId, memberId)
				.orElseThrow(() -> new CustomException(ErrorCode.CHECKLIST_NOT_FOUND));
	}
}
