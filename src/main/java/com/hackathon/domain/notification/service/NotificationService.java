package com.hackathon.domain.notification.service;

import com.hackathon.domain.bookmark.entity.Bookmark;
import com.hackathon.domain.notification.dto.NotificationDto.NotificationGroupResponse;
import com.hackathon.domain.notification.dto.NotificationDto.NotificationHistoryResponse;
import com.hackathon.domain.notification.dto.NotificationDto.NotificationItemResponse;
import com.hackathon.domain.notification.entity.Notification;
import com.hackathon.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

	private final NotificationRepository notificationRepository;

	public NotificationHistoryResponse getNotifications(Long memberId) {
		List<Notification> notifications = notificationRepository.findNotificationHistory(memberId);
		LinkedHashMap<Long, NotificationGroupAccumulator> groupedNotifications = new LinkedHashMap<>();

		for (Notification notification : notifications) {
			Bookmark bookmark = notification.getBookmark();
			NotificationGroupAccumulator accumulator = groupedNotifications.computeIfAbsent(
					bookmark.getId(),
					bookmarkId -> new NotificationGroupAccumulator(
							bookmark.getId(),
							bookmark.getTitle(),
							bookmark.getViewCount() != null && bookmark.getViewCount() > 0,
							notification.getCreatedAt()
					)
			);
			accumulator.addNotification(notification);
		}

		List<NotificationGroupResponse> notificationGroups = groupedNotifications.values().stream()
				.map(NotificationGroupAccumulator::toResponse)
				.toList();

		return new NotificationHistoryResponse(
				notificationGroups.size(),
				notifications.size(),
				notificationGroups
		);
	}

	private static class NotificationGroupAccumulator {

		private final Long bookmarkId;
		private final String bookmarkTitle;
		private final Boolean isRead;
		private final LocalDateTime latestNotificationAt;
		private final List<NotificationItemResponse> notificationItems = new ArrayList<>();
		private int notificationCount;

		private NotificationGroupAccumulator(
				Long bookmarkId,
				String bookmarkTitle,
				Boolean isRead,
				LocalDateTime latestNotificationAt
		) {
			this.bookmarkId = bookmarkId;
			this.bookmarkTitle = bookmarkTitle;
			this.isRead = isRead;
			this.latestNotificationAt = latestNotificationAt;
		}

		private void addNotification(Notification notification) {
			notificationCount++;
			notificationItems.add(new NotificationItemResponse(
					notification.getId(),
					notification.getTitle(),
					notification.getMessage(),
					notification.getCreatedAt()
			));
		}

		private NotificationGroupResponse toResponse() {
			return new NotificationGroupResponse(
					bookmarkId,
					bookmarkTitle,
					notificationCount,
					isRead,
					latestNotificationAt,
					notificationItems
			);
		}
	}
}
