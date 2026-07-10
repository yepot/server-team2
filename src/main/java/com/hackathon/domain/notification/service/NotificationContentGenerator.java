package com.hackathon.domain.notification.service;

import java.util.List;

public interface NotificationContentGenerator {

	GeneratedNotificationContent generate(GenerationRequest request);

	record GenerationRequest(
			String bookmarkTitle,
			List<String> incompleteChecklists,
			int notificationCount,
			int reminderLevel,
			String instruction
	) {}

	record GeneratedNotificationContent(
			String title,
			String message
	) {}
}
