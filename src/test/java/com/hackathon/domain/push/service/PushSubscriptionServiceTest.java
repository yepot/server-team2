package com.hackathon.domain.push.service;

import com.hackathon.domain.member.entity.Member;
import com.hackathon.domain.member.repository.MemberRepository;
import com.hackathon.domain.push.config.PushVapidProperties;
import com.hackathon.domain.push.dto.PushDto.SubscriptionDeleteRequest;
import com.hackathon.domain.push.dto.PushDto.SubscriptionRequest;
import com.hackathon.domain.push.dto.PushDto.SubscriptionResponse;
import com.hackathon.domain.push.dto.PushDto.VapidPublicKeyResponse;
import com.hackathon.domain.push.entity.PushSubscription;
import com.hackathon.domain.push.repository.PushSubscriptionRepository;
import com.hackathon.global.exception.CustomException;
import com.hackathon.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PushSubscriptionServiceTest {

	@Mock
	private PushSubscriptionRepository pushSubscriptionRepository;

	@Mock
	private MemberRepository memberRepository;

	private final PushVapidProperties configuredProperties = new PushVapidProperties(
			"public-key",
			"private-key",
			"mailto:test@example.com",
			300
	);

	private PushSubscriptionService pushSubscriptionService;

	@BeforeEach
	void setUp() {
		pushSubscriptionService = new PushSubscriptionService(
				pushSubscriptionRepository,
				memberRepository,
				configuredProperties
		);
	}

	@Test
	void getVapidPublicKeyReturnsConfiguredKey() {
		Member member = createMember(1L);
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));

		VapidPublicKeyResponse response = pushSubscriptionService.getVapidPublicKey(1L);

		assertThat(response.publicKey()).isEqualTo("public-key");
	}

	@Test
	void getVapidPublicKeyThrowsWhenPushNotConfigured() {
		PushSubscriptionService serviceWithoutConfig = new PushSubscriptionService(
				pushSubscriptionRepository,
				memberRepository,
				new PushVapidProperties(null, null, null, 300)
		);
		given(memberRepository.findById(1L)).willReturn(Optional.of(createMember(1L)));

		assertThatThrownBy(() -> serviceWithoutConfig.getVapidPublicKey(1L))
				.isInstanceOfSatisfying(CustomException.class,
						exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PUSH_NOT_CONFIGURED));
	}

	@Test
	void saveSubscriptionCreatesNewSubscription() {
		Member member = createMember(1L);
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(pushSubscriptionRepository.findByEndpoint("https://example.com/push/1")).willReturn(Optional.empty());
		given(pushSubscriptionRepository.save(any(PushSubscription.class))).willAnswer(invocation -> {
			PushSubscription pushSubscription = invocation.getArgument(0);
			ReflectionTestUtils.setField(pushSubscription, "id", 10L);
			ReflectionTestUtils.invokeMethod(pushSubscription, "onCreate");
			return pushSubscription;
		});

		SubscriptionResponse response = pushSubscriptionService.saveSubscription(
				1L,
				new SubscriptionRequest("https://example.com/push/1", "p256dh-key", "auth-key")
		);

		assertThat(response.subscriptionId()).isEqualTo(10L);
		assertThat(response.endpoint()).isEqualTo("https://example.com/push/1");
		verify(pushSubscriptionRepository).save(any(PushSubscription.class));
		verify(pushSubscriptionRepository).flush();
	}

	@Test
	void saveSubscriptionUpdatesExistingEndpoint() {
		Member member = createMember(2L);
		PushSubscription pushSubscription = createPushSubscription(10L, createMember(1L), "https://example.com/push/1");
		given(memberRepository.findById(2L)).willReturn(Optional.of(member));
		given(pushSubscriptionRepository.findByEndpoint("https://example.com/push/1"))
				.willReturn(Optional.of(pushSubscription));

		SubscriptionResponse response = pushSubscriptionService.saveSubscription(
				2L,
				new SubscriptionRequest("https://example.com/push/1", "updated-p256dh", "updated-auth")
		);

		assertThat(response.subscriptionId()).isEqualTo(10L);
		assertThat(pushSubscription.getMember().getId()).isEqualTo(2L);
		assertThat(pushSubscription.getP256dh()).isEqualTo("updated-p256dh");
		assertThat(pushSubscription.getAuth()).isEqualTo("updated-auth");
		verify(pushSubscriptionRepository, never()).save(any(PushSubscription.class));
		verify(pushSubscriptionRepository).flush();
	}

	@Test
	void deleteSubscriptionRemovesOwnedSubscription() {
		Member member = createMember(1L);
		PushSubscription pushSubscription = createPushSubscription(10L, member, "https://example.com/push/1");
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(pushSubscriptionRepository.findByMember_IdAndEndpoint(1L, "https://example.com/push/1"))
				.willReturn(Optional.of(pushSubscription));

		pushSubscriptionService.deleteSubscription(1L, new SubscriptionDeleteRequest("https://example.com/push/1"));

		verify(pushSubscriptionRepository).delete(pushSubscription);
	}

	private Member createMember(Long memberId) {
		Member member = Member.builder()
				.loginId("user-" + memberId)
				.password("encoded-password")
				.nickname("회원")
				.totalScore(0)
				.build();
		ReflectionTestUtils.setField(member, "id", memberId);
		return member;
	}

	private PushSubscription createPushSubscription(Long subscriptionId, Member member, String endpoint) {
		PushSubscription pushSubscription = new PushSubscription(
				member,
				endpoint,
				"p256dh-key",
				"auth-key"
		);
		ReflectionTestUtils.setField(pushSubscription, "id", subscriptionId);
		ReflectionTestUtils.invokeMethod(pushSubscription, "onCreate");
		return pushSubscription;
	}
}
