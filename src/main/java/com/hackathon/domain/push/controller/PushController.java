package com.hackathon.domain.push.controller;

import com.hackathon.domain.push.dto.PushDto.SubscriptionDeleteRequest;
import com.hackathon.domain.push.dto.PushDto.SubscriptionDeleteResponse;
import com.hackathon.domain.push.dto.PushDto.SubscriptionRequest;
import com.hackathon.domain.push.dto.PushDto.SubscriptionResponse;
import com.hackathon.domain.push.dto.PushDto.VapidPublicKeyResponse;
import com.hackathon.domain.push.service.PushSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/push")
@Tag(name = "Push", description = "PWA Web Push API")
public class PushController {

	private final PushSubscriptionService pushSubscriptionService;

	@GetMapping("/public-key")
	@Operation(summary = "VAPID 공개키 조회")
	public ResponseEntity<VapidPublicKeyResponse> getVapidPublicKey(
			@AuthenticationPrincipal Long memberId
	) {
		return ResponseEntity.ok(pushSubscriptionService.getVapidPublicKey(memberId));
	}

	@PostMapping("/subscriptions")
	@Operation(summary = "푸시 구독 저장")
	public ResponseEntity<SubscriptionResponse> saveSubscription(
			@AuthenticationPrincipal Long memberId,
			@Valid @RequestBody SubscriptionRequest request
	) {
		return ResponseEntity.ok(pushSubscriptionService.saveSubscription(memberId, request));
	}

	@DeleteMapping("/subscriptions")
	@Operation(summary = "푸시 구독 삭제")
	public ResponseEntity<SubscriptionDeleteResponse> deleteSubscription(
			@AuthenticationPrincipal Long memberId,
			@Valid @RequestBody SubscriptionDeleteRequest request
	) {
		return ResponseEntity.ok(pushSubscriptionService.deleteSubscription(memberId, request));
	}
}
