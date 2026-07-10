package com.hackathon.domain.notification.service;

import com.hackathon.domain.bookmark.entity.Bookmark;
import com.hackathon.domain.bookmark.repository.BookmarkRepository;
import com.hackathon.domain.checklist.repository.ChecklistRepository;
import com.hackathon.domain.notification.config.NotificationReminderProperties;
import com.hackathon.domain.notification.dto.NotificationDto.CreatedNotificationResponse;
import com.hackathon.domain.notification.dto.NotificationDto.NotificationGenerationResponse;
import com.hackathon.domain.notification.entity.Notification;
import com.hackathon.domain.notification.repository.NotificationRepository;
import com.hackathon.domain.notification.service.NotificationContentGenerator.GeneratedNotificationContent;
import com.hackathon.domain.notification.service.NotificationContentGenerator.GenerationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationProcessingService {

	private final BookmarkRepository bookmarkRepository;
	private final ChecklistRepository checklistRepository;
	private final NotificationRepository notificationRepository;
	private final NotificationContentGenerator notificationContentGenerator;
	private final NotificationReminderProperties notificationReminderProperties;
	private final ApplicationEventPublisher applicationEventPublisher;

	@Transactional
	public NotificationGenerationResponse generateDueNotifications() {
		return generateDueNotifications(LocalDateTime.now());
	}

	@Transactional
	public NotificationGenerationResponse generateDueNotifications(LocalDateTime now) {
		List<Bookmark> dueBookmarks = bookmarkRepository.findDueReminderBookmarks(
				now,
				now.minusMinutes(notificationReminderProperties.intervalMinutes())
		);
		List<CreatedNotificationResponse> createdNotifications = new ArrayList<>();

		for (Bookmark bookmark : dueBookmarks) {
			List<String> incompleteChecklists = checklistRepository.findIncompleteContentsByBookmarkId(bookmark.getId());
			if (incompleteChecklists.isEmpty()) {
				continue;
			}

			int notificationCount = Math.toIntExact(notificationRepository.countByBookmark_Id(bookmark.getId()) + 1);
			int reminderLevel = calculateReminderLevel(notificationCount);
			GeneratedNotificationContent generatedContent = notificationContentGenerator.generate(
					new GenerationRequest(
							bookmark.getTitle(),
							incompleteChecklists,
							notificationCount,
							reminderLevel,
							notificationReminderProperties.instruction()
					)
			);

			Notification notification = notificationRepository.saveAndFlush(
					Notification.builder()
							.member(bookmark.getMemberId())
							.bookmark(bookmark)
							.title(generatedContent.title())
							.message(generatedContent.message())
							.build()
			);
			applicationEventPublisher.publishEvent(new NotificationCreatedEvent(
					bookmark.getMemberId().getId(),
					notification.getId(),
					bookmark.getId(),
					notification.getTitle(),
					notification.getMessage(),
					notificationCount,
					notification.getCreatedAt()
			));

			createdNotifications.add(new CreatedNotificationResponse(
					notification.getId(),
					bookmark.getId(),
					bookmark.getTitle(),
					notificationCount,
					reminderLevel,
					notification.getTitle(),
					notification.getMessage(),
					incompleteChecklists.size(),
					notification.getCreatedAt().plusMinutes(notificationReminderProperties.intervalMinutes()),
					notification.getCreatedAt()
			));
		}

		return new NotificationGenerationResponse(
				dueBookmarks.size(),
				createdNotifications.size(),
				createdNotifications,
				resolveMessage(dueBookmarks.size(), createdNotifications.size())
		);
	}

	private int calculateReminderLevel(int notificationCount) {
		return Math.min(notificationCount, 3);
	}

	private String resolveMessage(int processedBookmarkCount, int createdNotificationCount) {
		if (processedBookmarkCount > 0 && createdNotificationCount == 0) {
			return "모든 체크리스트가 완료되어 추가 알림을 생성하지 않았습니다.";
		}
		return null;
	}
}
