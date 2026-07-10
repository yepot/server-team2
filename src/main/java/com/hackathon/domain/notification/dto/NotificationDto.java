package com.hackathon.domain.notification.dto;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationDto {

	public record NotificationHistoryResponse(
			int totalBookmarkCount,
			int totalNotificationCount,
			List<NotificationGroupResponse> notifications
	) {}

	public record NotificationGroupResponse(
			Long bookmarkId,
			String bookmarkTitle,
			int notificationCount,
			Boolean isRead,
			LocalDateTime latestNotificationAt,
			List<NotificationItemResponse> notificationItems
	) {}

	public record NotificationItemResponse(
			Long notificationId,
			String title,
			String message,
			LocalDateTime sentAt
	) {}
}
