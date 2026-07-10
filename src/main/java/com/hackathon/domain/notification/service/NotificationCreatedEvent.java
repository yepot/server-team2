package com.hackathon.domain.notification.service;

import java.time.LocalDateTime;

public record NotificationCreatedEvent(
		Long memberId,
		Long notificationId,
		Long bookmarkId,
		String title,
		String message,
		int notificationCount,
		LocalDateTime createdAt
) {}
