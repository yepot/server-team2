package com.hackathon.domain.push.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "push.vapid")
public record PushVapidProperties(
		String publicKey,
		String privateKey,
		String subject,
		int ttlSeconds
) {
	public PushVapidProperties {
		publicKey = normalize(publicKey);
		privateKey = normalize(privateKey);
		subject = normalize(subject);
		ttlSeconds = ttlSeconds <= 0 ? 300 : ttlSeconds;
	}

	public boolean isConfigured() {
		return StringUtils.hasText(publicKey)
				&& StringUtils.hasText(privateKey)
				&& StringUtils.hasText(subject);
	}

	private static String normalize(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
