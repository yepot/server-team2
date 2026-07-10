package com.hackathon.domain.push.service;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.hackathon.domain.member.entity.Member;
import com.hackathon.domain.notification.service.NotificationCreatedEvent;
import com.hackathon.domain.push.config.PushVapidProperties;
import com.hackathon.domain.push.entity.PushSubscription;
import com.hackathon.domain.push.repository.PushSubscriptionRepository;
import com.hackathon.domain.push.service.WebPushClient.WebPushSendResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PushNotificationServiceTest {

	@Mock
	private PushSubscriptionRepository pushSubscriptionRepository;

	@Mock
	private WebPushClient webPushClient;

	@Test
	void handleNotificationCreatedSkipsWhenPushNotConfigured() {
		PushNotificationService pushNotificationService = new PushNotificationService(
				pushSubscriptionRepository,
				new PushVapidProperties(null, null, null, 300),
				webPushClient,
				JsonMapper.builder().findAndAddModules().build()
		);

		pushNotificationService.handleNotificationCreated(createEvent());

		verifyNoInteractions(pushSubscriptionRepository);
		verifyNoInteractions(webPushClient);
	}

	@Test
	void handleNotificationCreatedSendsPayloadToSubscriptions() {
		PushSubscription pushSubscription = createPushSubscription(10L, 1L, "https://example.com/push/1");
		given(pushSubscriptionRepository.findAllByMember_Id(1L)).willReturn(List.of(pushSubscription));
		given(webPushClient.send(org.mockito.ArgumentMatchers.eq(pushSubscription), org.mockito.ArgumentMatchers.anyString()))
				.willReturn(new WebPushSendResult(201, null));
		PushNotificationService pushNotificationService = new PushNotificationService(
				pushSubscriptionRepository,
				new PushVapidProperties("public-key", "private-key", "mailto:test@example.com", 300),
				webPushClient,
				JsonMapper.builder().findAndAddModules().build()
		);

		pushNotificationService.handleNotificationCreated(createEvent());

		ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
		verify(webPushClient).send(org.mockito.ArgumentMatchers.eq(pushSubscription), payloadCaptor.capture());
		assertThat(payloadCaptor.getValue()).contains("\"notificationId\":101");
		assertThat(payloadCaptor.getValue()).contains("\"bookmarkId\":10");
		assertThat(payloadCaptor.getValue()).contains("\"title\":\"세 번째 알림이에요!\"");
		verify(pushSubscriptionRepository, never()).delete(pushSubscription);
	}

	@Test
	void handleNotificationCreatedDeletesExpiredSubscription() {
		PushSubscription pushSubscription = createPushSubscription(10L, 1L, "https://example.com/push/1");
		given(pushSubscriptionRepository.findAllByMember_Id(1L)).willReturn(List.of(pushSubscription));
		given(webPushClient.send(org.mockito.ArgumentMatchers.eq(pushSubscription), org.mockito.ArgumentMatchers.anyString()))
				.willReturn(new WebPushSendResult(410, "expired"));
		PushNotificationService pushNotificationService = new PushNotificationService(
				pushSubscriptionRepository,
				new PushVapidProperties("public-key", "private-key", "mailto:test@example.com", 300),
				webPushClient,
				JsonMapper.builder().findAndAddModules().build()
		);

		pushNotificationService.handleNotificationCreated(createEvent());

		verify(pushSubscriptionRepository).delete(pushSubscription);
	}

	private NotificationCreatedEvent createEvent() {
		return new NotificationCreatedEvent(
				1L,
				101L,
				10L,
				"세 번째 알림이에요!",
				"지금 체크리스트 하나라도 완료해 주세요!",
				3,
				LocalDateTime.of(2026, 7, 11, 18, 10, 0)
		);
	}

	private PushSubscription createPushSubscription(Long subscriptionId, Long memberId, String endpoint) {
		Member member = Member.builder()
				.loginId("user-" + memberId)
				.password("encoded-password")
				.nickname("회원")
				.totalScore(0)
				.build();
		ReflectionTestUtils.setField(member, "id", memberId);

		PushSubscription pushSubscription = new PushSubscription(member, endpoint, "p256dh-key", "auth-key");
		ReflectionTestUtils.setField(pushSubscription, "id", subscriptionId);
		return pushSubscription;
	}
}
