package com.hackathon.domain.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification.ai")
public record NotificationAiProperties(
		String baseUrl,
		String apiKey,
		String model
) {
	public NotificationAiProperties {
		baseUrl = (baseUrl == null || baseUrl.isBlank()) ? "https://api.openai.com/v1" : baseUrl;
		model = (model == null || model.isBlank()) ? "gpt-5-mini" : model;
	}
}
