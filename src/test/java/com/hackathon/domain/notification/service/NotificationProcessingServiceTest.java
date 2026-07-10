package com.hackathon.domain.notification.service;

import com.hackathon.domain.bookmark.entity.Bookmark;
import com.hackathon.domain.bookmark.repository.BookmarkRepository;
import com.hackathon.domain.checklist.repository.ChecklistRepository;
import com.hackathon.domain.member.entity.Member;
import com.hackathon.domain.notification.config.NotificationReminderProperties;
import com.hackathon.domain.notification.dto.NotificationDto.CreatedNotificationResponse;
import com.hackathon.domain.notification.dto.NotificationDto.NotificationGenerationResponse;
import com.hackathon.domain.notification.entity.Notification;
import com.hackathon.domain.notification.repository.NotificationRepository;
import com.hackathon.domain.notification.service.NotificationContentGenerator.GeneratedNotificationContent;
import com.hackathon.domain.notification.service.NotificationContentGenerator.GenerationRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class NotificationProcessingServiceTest {

	@Mock
	private BookmarkRepository bookmarkRepository;

	@Mock
	private ChecklistRepository checklistRepository;

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private NotificationContentGenerator notificationContentGenerator;

	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	private NotificationProcessingService notificationProcessingService;

	@BeforeEach
	void setUp() {
		notificationProcessingService = new NotificationProcessingService(
				bookmarkRepository,
				checklistRepository,
				notificationRepository,
				notificationContentGenerator,
				new NotificationReminderProperties(5, "재미있고 재촉하는 알림을 작성해 주세요."),
				applicationEventPublisher
		);
	}

	@Test
	void generateDueNotificationsCreatesFirstReminder() {
		LocalDateTime now = LocalDateTime.of(2026, 7, 11, 18, 0, 0);
		Bookmark bookmark = createBookmark(10L, 1L, "@@회사 채용 공고");
		given(bookmarkRepository.findDueReminderBookmarks(now, now.minusMinutes(5))).willReturn(List.of(bookmark));
		given(checklistRepository.findIncompleteContentsByBookmarkId(10L)).willReturn(
				List.of("자기소개서 작성하기", "이력서 검토하기", "지원서 제출하기")
		);
		given(notificationRepository.countByBookmark_Id(10L)).willReturn(0L);
		given(notificationContentGenerator.generate(any(GenerationRequest.class))).willReturn(
				new GeneratedNotificationContent(
						"지원 준비를 시작할 시간이에요!",
						"저장해 둔 공고가 기다리고 있어요. 자기소개서 초안부터 가볍게 시작해볼까요?"
				)
		);
		given(notificationRepository.saveAndFlush(any(Notification.class))).willAnswer(invocation -> {
			Notification notification = invocation.getArgument(0);
			ReflectionTestUtils.setField(notification, "id", 101L);
			ReflectionTestUtils.setField(notification, "createdAt", now);
			return notification;
		});

		NotificationGenerationResponse response = notificationProcessingService.generateDueNotifications(now);

		assertThat(response.processedBookmarkCount()).isEqualTo(1);
		assertThat(response.createdNotificationCount()).isEqualTo(1);
		assertThat(response.message()).isNull();

		CreatedNotificationResponse createdNotification = response.notifications().get(0);
		assertThat(createdNotification.notificationId()).isEqualTo(101L);
		assertThat(createdNotification.bookmarkId()).isEqualTo(10L);
		assertThat(createdNotification.notificationCount()).isEqualTo(1);
		assertThat(createdNotification.reminderLevel()).isEqualTo(1);
		assertThat(createdNotification.incompleteChecklistCount()).isEqualTo(3);
		assertThat(createdNotification.nextReminderAt()).isEqualTo(now.plusMinutes(5));
		assertThat(createdNotification.createdAt()).isEqualTo(now);

		ArgumentCaptor<GenerationRequest> requestCaptor = ArgumentCaptor.forClass(GenerationRequest.class);
		org.mockito.Mockito.verify(notificationContentGenerator).generate(requestCaptor.capture());
		assertThat(requestCaptor.getValue().bookmarkTitle()).isEqualTo("@@회사 채용 공고");
		assertThat(requestCaptor.getValue().notificationCount()).isEqualTo(1);
		assertThat(requestCaptor.getValue().reminderLevel()).isEqualTo(1);

		ArgumentCaptor<NotificationCreatedEvent> eventCaptor = ArgumentCaptor.forClass(NotificationCreatedEvent.class);
		org.mockito.Mockito.verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
		assertThat(eventCaptor.getValue().memberId()).isEqualTo(1L);
		assertThat(eventCaptor.getValue().notificationId()).isEqualTo(101L);
		assertThat(eventCaptor.getValue().bookmarkId()).isEqualTo(10L);
		assertThat(eventCaptor.getValue().notificationCount()).isEqualTo(1);
	}

	@Test
	void generateDueNotificationsRaisesReminderLevelForRepeatedReminder() {
		LocalDateTime now = LocalDateTime.of(2026, 7, 11, 18, 0, 0);
		Bookmark bookmark = createBookmark(20L, 1L, "Spring Batch 학습 자료");
		given(bookmarkRepository.findDueReminderBookmarks(now, now.minusMinutes(5))).willReturn(List.of(bookmark));
		given(checklistRepository.findIncompleteContentsByBookmarkId(20L)).willReturn(
				List.of("예제 실행하기", "정리 노트 작성하기")
		);
		given(notificationRepository.countByBookmark_Id(20L)).willReturn(2L);
		given(notificationContentGenerator.generate(any(GenerationRequest.class))).willReturn(
				new GeneratedNotificationContent(
						"아직도 체크리스트가 남아 있어요!",
						"벌써 세 번째 알림이에요. 예제 실행 하나라도 지금 완료하고 체크해 주세요!"
				)
		);
		given(notificationRepository.saveAndFlush(any(Notification.class))).willAnswer(invocation -> {
			Notification notification = invocation.getArgument(0);
			ReflectionTestUtils.setField(notification, "id", 102L);
			ReflectionTestUtils.setField(notification, "createdAt", now);
			return notification;
		});

		NotificationGenerationResponse response = notificationProcessingService.generateDueNotifications(now);

		assertThat(response.processedBookmarkCount()).isEqualTo(1);
		assertThat(response.createdNotificationCount()).isEqualTo(1);
		assertThat(response.notifications().get(0).notificationCount()).isEqualTo(3);
		assertThat(response.notifications().get(0).reminderLevel()).isEqualTo(3);

		ArgumentCaptor<GenerationRequest> requestCaptor = ArgumentCaptor.forClass(GenerationRequest.class);
		org.mockito.Mockito.verify(notificationContentGenerator).generate(requestCaptor.capture());
		assertThat(requestCaptor.getValue().notificationCount()).isEqualTo(3);
		assertThat(requestCaptor.getValue().reminderLevel()).isEqualTo(3);
	}

	@Test
	void generateDueNotificationsSkipsWhenAllChecklistsAreCompleted() {
		LocalDateTime now = LocalDateTime.of(2026, 7, 11, 18, 0, 0);
		Bookmark bookmark = createBookmark(30L, 1L, "완료된 북마크");
		given(bookmarkRepository.findDueReminderBookmarks(now, now.minusMinutes(5))).willReturn(List.of(bookmark));
		given(checklistRepository.findIncompleteContentsByBookmarkId(30L)).willReturn(List.of());

		NotificationGenerationResponse response = notificationProcessingService.generateDueNotifications(now);

		assertThat(response.processedBookmarkCount()).isEqualTo(1);
		assertThat(response.createdNotificationCount()).isZero();
		assertThat(response.notifications()).isEmpty();
		assertThat(response.message()).isEqualTo("모든 체크리스트가 완료되어 추가 알림을 생성하지 않았습니다.");
		verifyNoInteractions(notificationContentGenerator);
		verifyNoInteractions(applicationEventPublisher);
	}

	private Bookmark createBookmark(Long bookmarkId, Long memberId, String title) {
		Member member = Member.builder()
				.loginId("yepot")
				.password("encoded-password")
				.nickname("은서")
				.totalScore(0)
				.build();
		ReflectionTestUtils.setField(member, "id", memberId);

		Bookmark bookmark = Bookmark.builder()
				.memberId(member)
				.url("https://example.com")
				.title(title)
				.status("ACTIVE")
				.remindAt(LocalDateTime.of(2026, 7, 11, 18, 0, 0))
				.build();
		ReflectionTestUtils.setField(bookmark, "id", bookmarkId);
		return bookmark;
	}
}
