package com.hackathon.domain.push.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.domain.notification.service.NotificationCreatedEvent;
import com.hackathon.domain.push.config.PushVapidProperties;
import com.hackathon.domain.push.entity.PushSubscription;
import com.hackathon.domain.push.repository.PushSubscriptionRepository;
import com.hackathon.domain.push.service.WebPushClient.WebPushSendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

	private final PushSubscriptionRepository pushSubscriptionRepository;
	private final PushVapidProperties pushVapidProperties;
	private final WebPushClient webPushClient;
	private final ObjectMapper objectMapper;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleNotificationCreated(NotificationCreatedEvent event) {
		if (!pushVapidProperties.isConfigured()) {
			log.debug("Web Push is not configured. Skip push delivery.");
			return;
		}

		List<PushSubscription> pushSubscriptions = pushSubscriptionRepository.findAllByMember_Id(event.memberId());
		if (pushSubscriptions.isEmpty()) {
			return;
		}

		String payload = serializePayload(new PushNotificationPayload(
				event.notificationId(),
				event.bookmarkId(),
				event.title(),
				event.message(),
				event.notificationCount(),
				event.createdAt()
		));

		for (PushSubscription pushSubscription : pushSubscriptions) {
			sendPushNotification(pushSubscription, payload, event.notificationId());
		}
	}

	private void sendPushNotification(PushSubscription pushSubscription, String payload, Long notificationId) {
		try {
			WebPushSendResult result = webPushClient.send(pushSubscription, payload);
			if (result.shouldDeleteSubscription()) {
				pushSubscriptionRepository.delete(pushSubscription);
				log.info(
						"Removed expired push subscription. subscriptionId={}, notificationId={}, statusCode={}",
						pushSubscription.getId(),
						notificationId,
						result.statusCode()
				);
				return;
			}

			if (!result.isSuccessful()) {
				log.warn(
						"Web Push delivery failed. subscriptionId={}, notificationId={}, statusCode={}, responseBody={}",
						pushSubscription.getId(),
						notificationId,
						result.statusCode(),
						result.responseBody()
				);
			}
		} catch (RuntimeException exception) {
			log.error(
					"Web Push delivery threw an exception. subscriptionId={}, notificationId={}",
					pushSubscription.getId(),
					notificationId,
					exception
			);
		}
	}

	private String serializePayload(PushNotificationPayload payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Web Push payload 직렬화에 실패했습니다.", exception);
		}
	}

	private record PushNotificationPayload(
			Long notificationId,
			Long bookmarkId,
			String title,
			String message,
			int notificationCount,
			LocalDateTime createdAt
	) {}
}
