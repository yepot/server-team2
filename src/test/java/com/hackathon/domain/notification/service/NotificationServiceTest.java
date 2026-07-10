package com.hackathon.domain.notification.service;

import com.hackathon.domain.bookmark.entity.Bookmark;
import com.hackathon.domain.member.entity.Member;
import com.hackathon.domain.notification.dto.NotificationDto.NotificationGroupResponse;
import com.hackathon.domain.notification.dto.NotificationDto.NotificationHistoryResponse;
import com.hackathon.domain.notification.dto.NotificationDto.NotificationItemResponse;
import com.hackathon.domain.notification.entity.Notification;
import com.hackathon.domain.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@Mock
	private NotificationRepository notificationRepository;

	@InjectMocks
	private NotificationService notificationService;

	@Test
	void getNotificationsGroupsByBookmarkAndCountsItems() {
		Bookmark firstBookmark = createBookmark(10L, 1L, "공고", 1);
		Bookmark secondBookmark = createBookmark(20L, 1L, "Spring Batch 학습 자료", 0);

		Notification notification103 = createNotification(
				103L,
				firstBookmark,
				"체크리스트 리마인드",
				"자기소개서 작성을 완료할 시간이에요.",
				LocalDateTime.of(2026, 7, 10, 18, 0, 0)
		);
		Notification notification105 = createNotification(
				105L,
				secondBookmark,
				"학습 리마인드",
				"Spring Batch 예제를 실행해볼 시간이에요.",
				LocalDateTime.of(2026, 7, 10, 15, 0, 0)
		);
		Notification notification102 = createNotification(
				102L,
				firstBookmark,
				"체크리스트 리마인드",
				"지원서 제출 전 내용을 검토해보세요.",
				LocalDateTime.of(2026, 7, 9, 18, 0, 0)
		);
		Notification notification104 = createNotification(
				104L,
				secondBookmark,
				"학습 리마인드",
				"저장한 학습 자료를 확인해보세요.",
				LocalDateTime.of(2026, 7, 9, 15, 0, 0)
		);
		Notification notification101 = createNotification(
				101L,
				firstBookmark,
				"체크리스트 리마인드",
				"오늘 지원서를 작성해보세요.",
				LocalDateTime.of(2026, 7, 8, 18, 0, 0)
		);

		given(notificationRepository.findNotificationHistory(1L)).willReturn(
				List.of(notification103, notification105, notification102, notification104, notification101)
		);

		NotificationHistoryResponse response = notificationService.getNotifications(1L);

		assertThat(response.totalBookmarkCount()).isEqualTo(2);
		assertThat(response.totalNotificationCount()).isEqualTo(5);
		assertThat(response.notifications()).hasSize(2);

		NotificationGroupResponse firstGroup = response.notifications().get(0);
		assertThat(firstGroup.bookmarkId()).isEqualTo(10L);
		assertThat(firstGroup.bookmarkTitle()).isEqualTo("공고");
		assertThat(firstGroup.notificationCount()).isEqualTo(3);
		assertThat(firstGroup.isRead()).isTrue();
		assertThat(firstGroup.latestNotificationAt()).isEqualTo(LocalDateTime.of(2026, 7, 10, 18, 0, 0));
		assertThat(firstGroup.notificationItems()).extracting(NotificationItemResponse::notificationId)
				.containsExactly(103L, 102L, 101L);

		NotificationGroupResponse secondGroup = response.notifications().get(1);
		assertThat(secondGroup.bookmarkId()).isEqualTo(20L);
		assertThat(secondGroup.bookmarkTitle()).isEqualTo("Spring Batch 학습 자료");
		assertThat(secondGroup.notificationCount()).isEqualTo(2);
		assertThat(secondGroup.isRead()).isFalse();
		assertThat(secondGroup.latestNotificationAt()).isEqualTo(LocalDateTime.of(2026, 7, 10, 15, 0, 0));
		assertThat(secondGroup.notificationItems()).extracting(NotificationItemResponse::notificationId)
				.containsExactly(105L, 104L);
	}

	@Test
	void getNotificationsReturnsEmptyResponseWhenHistoryDoesNotExist() {
		given(notificationRepository.findNotificationHistory(1L)).willReturn(List.of());

		NotificationHistoryResponse response = notificationService.getNotifications(1L);

		assertThat(response.totalBookmarkCount()).isZero();
		assertThat(response.totalNotificationCount()).isZero();
		assertThat(response.notifications()).isEmpty();
	}

	private Bookmark createBookmark(Long bookmarkId, Long memberId, String title, int viewCount) {
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
				.remindAt(null)
				.build();
		ReflectionTestUtils.setField(bookmark, "id", bookmarkId);
		ReflectionTestUtils.setField(bookmark, "viewCount", viewCount);
		return bookmark;
	}

	private Notification createNotification(
			Long notificationId,
			Bookmark bookmark,
			String title,
			String message,
			LocalDateTime sentAt
	) {
		Notification notification = Notification.builder()
				.member((Member) ReflectionTestUtils.getField(bookmark, "memberId"))
				.bookmark(bookmark)
				.title(title)
				.message(message)
				.build();
		ReflectionTestUtils.setField(notification, "id", notificationId);
		ReflectionTestUtils.setField(notification, "createdAt", sentAt);
		return notification;
	}
}
