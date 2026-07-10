package com.hackathon.domain.push.service;

import com.hackathon.domain.push.entity.PushSubscription;

public interface WebPushClient {

	WebPushSendResult send(PushSubscription pushSubscription, String payload);

	record WebPushSendResult(
			int statusCode,
			String responseBody
	) {
		public boolean isSuccessful() {
			return statusCode >= 200 && statusCode < 300;
		}

		public boolean shouldDeleteSubscription() {
			return statusCode == 404 || statusCode == 410;
		}
	}
}
