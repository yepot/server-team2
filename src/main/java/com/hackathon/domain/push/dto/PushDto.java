package com.hackathon.domain.push.dto;

import com.hackathon.domain.push.entity.PushSubscription;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class PushDto {

	public record VapidPublicKeyResponse(
			String publicKey
	) {}

	public record SubscriptionRequest(
			@NotBlank(message = "endpoint는 필수입니다.")
			@Size(max = 5000, message = "endpoint 길이가 너무 깁니다.")
			String endpoint,

			@NotBlank(message = "p256dh는 필수입니다.")
			@Size(max = 512, message = "p256dh 길이가 너무 깁니다.")
			String p256dh,

			@NotBlank(message = "auth는 필수입니다.")
			@Size(max = 255, message = "auth 길이가 너무 깁니다.")
			String auth
	) {}

	public record SubscriptionResponse(
			Long subscriptionId,
			String endpoint,
			LocalDateTime createdAt,
			LocalDateTime updatedAt
	) {
		public static SubscriptionResponse from(PushSubscription pushSubscription) {
			return new SubscriptionResponse(
					pushSubscription.getId(),
					pushSubscription.getEndpoint(),
					pushSubscription.getCreatedAt(),
					pushSubscription.getUpdatedAt()
			);
		}
	}

	public record SubscriptionDeleteRequest(
			@NotBlank(message = "endpoint는 필수입니다.")
			@Size(max = 5000, message = "endpoint 길이가 너무 깁니다.")
			String endpoint
	) {}

	public record SubscriptionDeleteResponse(
			String message
	) {}
}
