package com.hackathon.domain.score.service;

import com.hackathon.domain.bookmark.entity.Bookmark;
import com.hackathon.domain.checklist.entity.Checklist;
import com.hackathon.domain.checklist.repository.ChecklistRepository;
import com.hackathon.domain.member.entity.Member;
import com.hackathon.domain.member.repository.MemberRepository;
import com.hackathon.domain.notification.entity.Notification;
import com.hackathon.domain.notification.repository.NotificationRepository;
import com.hackathon.domain.score.entity.ScoreActionType;
import com.hackathon.domain.score.entity.ScoreHistory;
import com.hackathon.domain.score.repository.ScoreHistoryRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScoreAwardServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private ScoreHistoryRepository scoreHistoryRepository;

	@Mock
	private ChecklistRepository checklistRepository;

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private EntityManager entityManager;

	private ScoreService scoreService;

	@BeforeEach
	void setUp() {
		scoreService = new ScoreService(
				memberRepository,
				scoreHistoryRepository,
				checklistRepository,
				notificationRepository,
				entityManager
		);
	}

	@Test
	void awardChecklistCheckedAwardsChecklistAndBonusScores() {
		Member member = createMember(1L, 0);
		Bookmark bookmark = createBookmark(10L, member);
		Checklist checklist = createChecklist(100L, bookmark, true);
		Notification notification = createNotification(1000L, bookmark, member, LocalDateTime.now().minusHours(1));

		given(scoreHistoryRepository.existsByActionTypeAndChecklist_Id(ScoreActionType.CHECKLIST_ITEM_COMPLETED, 100L))
				.willReturn(false);
		given(checklistRepository.existsByBookmark_IdAndCheckedFalse(10L)).willReturn(false);
		given(scoreHistoryRepository.existsByActionTypeAndBookmark_Id(ScoreActionType.CHECKLIST_ALL_COMPLETED, 10L))
				.willReturn(false);
		given(scoreHistoryRepository.existsByActionTypeAndBookmark_Id(ScoreActionType.REMINDER_COMPLETED_WITHIN_24H, 10L))
				.willReturn(false);
		given(notificationRepository.findTopByBookmark_IdOrderByCreatedAtDescIdDesc(10L))
				.willReturn(Optional.of(notification));
		mockReferences(member, bookmark, checklist);

		scoreService.awardChecklistChecked(checklist);

		assertThat(member.getTotalScore()).isEqualTo(60);

		ArgumentCaptor<ScoreHistory> scoreHistoryCaptor = ArgumentCaptor.forClass(ScoreHistory.class);
		verify(scoreHistoryRepository, org.mockito.Mockito.times(3)).save(scoreHistoryCaptor.capture());
		assertThat(scoreHistoryCaptor.getAllValues()).extracting(ScoreHistory::getActionType)
				.containsExactly(
						ScoreActionType.CHECKLIST_ITEM_COMPLETED,
						ScoreActionType.CHECKLIST_ALL_COMPLETED,
						ScoreActionType.REMINDER_COMPLETED_WITHIN_24H
				);
	}

	@Test
	void awardChecklistCheckedSkipsWhenChecklistWasAlreadyRewarded() {
		Member member = createMember(1L, 0);
		Bookmark bookmark = createBookmark(10L, member);
		Checklist checklist = createChecklist(100L, bookmark, true);

		given(scoreHistoryRepository.existsByActionTypeAndChecklist_Id(ScoreActionType.CHECKLIST_ITEM_COMPLETED, 100L))
				.willReturn(true);
		given(checklistRepository.existsByBookmark_IdAndCheckedFalse(10L)).willReturn(true);

		scoreService.awardChecklistChecked(checklist);

		assertThat(member.getTotalScore()).isZero();
		verify(scoreHistoryRepository, never()).save(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void awardChecklistCheckedDoesNothingWhenUnchecked() {
		Member member = createMember(1L, 0);
		Bookmark bookmark = createBookmark(10L, member);
		Checklist checklist = createChecklist(100L, bookmark, false);

		scoreService.awardChecklistChecked(checklist);

		assertThat(member.getTotalScore()).isZero();
		verify(scoreHistoryRepository, never()).save(org.mockito.ArgumentMatchers.any());
	}

	private void mockReferences(Member member, Bookmark bookmark, Checklist checklist) {
		given(entityManager.getReference(Member.class, 1L)).willReturn(member);
		given(entityManager.getReference(Bookmark.class, 10L)).willReturn(bookmark);
		given(entityManager.getReference(Checklist.class, 100L)).willReturn(checklist);
	}

	private Member createMember(Long memberId, int totalScore) {
		Member member = Member.builder()
				.loginId("yepot")
				.password("encoded-password")
				.nickname("은서")
				.totalScore(totalScore)
				.build();
		ReflectionTestUtils.setField(member, "id", memberId);
		return member;
	}

	private Bookmark createBookmark(Long bookmarkId, Member member) {
		Bookmark bookmark = Bookmark.builder()
				.memberId(member)
				.url("https://example.com")
				.title("예시 북마크")
				.status("ACTIVE")
				.remindAt(LocalDateTime.now().minusDays(1))
				.build();
		ReflectionTestUtils.setField(bookmark, "id", bookmarkId);
		return bookmark;
	}

	private Checklist createChecklist(Long checklistId, Bookmark bookmark, boolean checked) {
		Checklist checklist = new Checklist(bookmark, "자기소개서 작성하기");
		ReflectionTestUtils.setField(checklist, "id", checklistId);
		ReflectionTestUtils.setField(checklist, "checked", checked);
		return checklist;
	}

	private Notification createNotification(Long notificationId, Bookmark bookmark, Member member, LocalDateTime createdAt) {
		Notification notification = Notification.builder()
				.member(member)
				.bookmark(bookmark)
				.title("리마인드")
				.message("확인해 주세요")
				.build();
		ReflectionTestUtils.setField(notification, "id", notificationId);
		ReflectionTestUtils.setField(notification, "createdAt", createdAt);
		return notification;
	}
}
