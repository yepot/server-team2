package com.hackathon.domain.push.service;

import com.hackathon.domain.member.entity.Member;
import com.hackathon.domain.member.repository.MemberRepository;
import com.hackathon.domain.push.config.PushVapidProperties;
import com.hackathon.domain.push.dto.PushDto.SubscriptionDeleteRequest;
import com.hackathon.domain.push.dto.PushDto.SubscriptionDeleteResponse;
import com.hackathon.domain.push.dto.PushDto.SubscriptionRequest;
import com.hackathon.domain.push.dto.PushDto.SubscriptionResponse;
import com.hackathon.domain.push.dto.PushDto.VapidPublicKeyResponse;
import com.hackathon.domain.push.entity.PushSubscription;
import com.hackathon.domain.push.repository.PushSubscriptionRepository;
import com.hackathon.global.exception.CustomException;
import com.hackathon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PushSubscriptionService {

	private final PushSubscriptionRepository pushSubscriptionRepository;
	private final MemberRepository memberRepository;
	private final PushVapidProperties pushVapidProperties;

	public VapidPublicKeyResponse getVapidPublicKey(Long memberId) {
		requireMemberId(memberId);
		validateMember(memberId);
		validatePushConfigured();

		return new VapidPublicKeyResponse(pushVapidProperties.publicKey());
	}

	@Transactional
	public SubscriptionResponse saveSubscription(Long memberId, SubscriptionRequest request) {
		requireMemberId(memberId);
		Member member = validateMember(memberId);

		PushSubscription pushSubscription = pushSubscriptionRepository.findByEndpoint(request.endpoint())
				.map(existingSubscription -> {
					existingSubscription.updateSubscription(member, request.p256dh(), request.auth());
					return existingSubscription;
				})
				.orElseGet(() -> pushSubscriptionRepository.save(
						new PushSubscription(member, request.endpoint(), request.p256dh(), request.auth())
				));

		pushSubscriptionRepository.flush();
		return SubscriptionResponse.from(pushSubscription);
	}

	@Transactional
	public SubscriptionDeleteResponse deleteSubscription(Long memberId, SubscriptionDeleteRequest request) {
		requireMemberId(memberId);
		validateMember(memberId);

		pushSubscriptionRepository.findByMember_IdAndEndpoint(memberId, request.endpoint())
				.ifPresent(pushSubscriptionRepository::delete);

		return new SubscriptionDeleteResponse("푸시 구독을 해제했습니다.");
	}

	private Member validateMember(Long memberId) {
		return memberRepository.findById(memberId)
				.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
	}

	private void validatePushConfigured() {
		if (!pushVapidProperties.isConfigured()) {
			throw new CustomException(ErrorCode.PUSH_NOT_CONFIGURED);
		}
	}

	private void requireMemberId(Long memberId) {
		if (memberId == null) {
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
	}
}
