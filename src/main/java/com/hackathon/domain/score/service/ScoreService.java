package com.hackathon.domain.score.service;

import com.hackathon.domain.bookmark.entity.Bookmark;
import com.hackathon.domain.checklist.entity.Checklist;
import com.hackathon.domain.checklist.repository.ChecklistRepository;
import com.hackathon.domain.member.entity.Member;
import com.hackathon.domain.member.repository.MemberRepository;
import com.hackathon.domain.notification.repository.NotificationRepository;
import com.hackathon.domain.score.dto.ScoreDto.ScoreResponse;
import com.hackathon.domain.score.entity.ScoreActionType;
import com.hackathon.domain.score.entity.ScoreHistory;
import com.hackathon.domain.score.repository.ScoreHistoryRepository;
import com.hackathon.global.exception.CustomException;
import com.hackathon.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScoreService {

	private final MemberRepository memberRepository;
	private final ScoreHistoryRepository scoreHistoryRepository;
	private final ChecklistRepository checklistRepository;
	private final NotificationRepository notificationRepository;
	private final EntityManager entityManager;

	public ScoreResponse getScore(Long memberId) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		return new ScoreResponse(String.valueOf(member.getTotalScore()));
	}

	@Transactional
	public boolean awardLinkSaved(Bookmark bookmark) {
		return awardBookmarkAction(bookmark, ScoreActionType.LINK_SAVED);
	}

	@Transactional
	public boolean awardPurposeSet(Bookmark bookmark) {
		return awardBookmarkAction(bookmark, ScoreActionType.PURPOSE_SET);
	}

	@Transactional
	public boolean awardTagSet(Bookmark bookmark) {
		return awardBookmarkAction(bookmark, ScoreActionType.TAG_SET);
	}

	@Transactional
	public boolean awardReminderSet(Bookmark bookmark) {
		return awardBookmarkAction(bookmark, ScoreActionType.REMINDER_SET);
	}

	@Transactional
	public boolean awardReminderRevisit(Bookmark bookmark) {
		return awardBookmarkAction(bookmark, ScoreActionType.REMINDER_REVISIT);
	}

	@Transactional
	public boolean awardRecommendedBookmarkCompleted(Bookmark bookmark) {
		return awardBookmarkAction(bookmark, ScoreActionType.RECOMMENDED_BOOKMARK_COMPLETED);
	}

	@Transactional
	public void awardChecklistChecked(Checklist checklist) {
		if (!checklist.isChecked()) {
			return;
		}

		awardChecklistAction(checklist, ScoreActionType.CHECKLIST_ITEM_COMPLETED);
		maybeAwardChecklistAllCompleted(checklist.getBookmark());
	}

	private void maybeAwardChecklistAllCompleted(Bookmark bookmark) {
		if (checklistRepository.existsByBookmark_IdAndCheckedFalse(bookmark.getId())) {
			return;
		}

		awardBookmarkAction(bookmark, ScoreActionType.CHECKLIST_ALL_COMPLETED);
		maybeAwardReminderCompletedWithin24Hours(bookmark);
	}

	private void maybeAwardReminderCompletedWithin24Hours(Bookmark bookmark) {
		if (scoreHistoryRepository.existsByActionTypeAndBookmark_Id(
				ScoreActionType.REMINDER_COMPLETED_WITHIN_24H,
				bookmark.getId()
		)) {
			return;
		}

		notificationRepository.findTopByBookmark_IdOrderByCreatedAtDescIdDesc(bookmark.getId())
				.filter(notification -> !notification.getCreatedAt().plusHours(24).isBefore(LocalDateTime.now()))
				.ifPresent(notification -> awardBookmarkAction(bookmark, ScoreActionType.REMINDER_COMPLETED_WITHIN_24H));
	}

	private boolean awardBookmarkAction(Bookmark bookmark, ScoreActionType actionType) {
		if (scoreHistoryRepository.existsByActionTypeAndBookmark_Id(actionType, bookmark.getId())) {
			return false;
		}

		saveScoreHistory(bookmark.getMemberId().getId(), bookmark.getId(), null, actionType);
		return true;
	}

	private boolean awardChecklistAction(Checklist checklist, ScoreActionType actionType) {
		if (scoreHistoryRepository.existsByActionTypeAndChecklist_Id(actionType, checklist.getId())) {
			return false;
		}

		saveScoreHistory(
				checklist.getBookmark().getMemberId().getId(),
				checklist.getBookmark().getId(),
				checklist.getId(),
				actionType
		);
		return true;
	}

	private void saveScoreHistory(
			Long memberId,
			Long bookmarkId,
			Long checklistId,
			ScoreActionType actionType
	) {
		Member member = entityManager.getReference(Member.class, memberId);
		member.addScore(actionType.getScore());

		ScoreHistory scoreHistory = ScoreHistory.builder()
				.member(member)
				.bookmark(bookmarkId != null ? entityManager.getReference(Bookmark.class, bookmarkId) : null)
				.checklist(checklistId != null ? entityManager.getReference(Checklist.class, checklistId) : null)
				.actionType(actionType)
				.score(actionType.getScore())
				.build();

		scoreHistoryRepository.save(scoreHistory);
	}
}
