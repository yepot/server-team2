package com.hackathon.domain.notification.service;

import com.hackathon.domain.notification.dto.NotificationDto.NotificationGenerationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "notification.scheduler", name = "enabled", havingValue = "true")
public class NotificationScheduler {

	private final NotificationProcessingService notificationProcessingService;

	@Scheduled(fixedDelayString = "${notification.scheduler.fixed-delay-ms:60000}")
	public void generateNotifications() {
		try {
			NotificationGenerationResponse response = notificationProcessingService.generateDueNotifications();
			if (response.processedBookmarkCount() > 0) {
				log.info(
						"Notification scheduler processed {} bookmarks and created {} notifications.",
						response.processedBookmarkCount(),
						response.createdNotificationCount()
				);
			}
		} catch (Exception exception) {
			log.error("Notification scheduler failed.", exception);
		}
	}
}
