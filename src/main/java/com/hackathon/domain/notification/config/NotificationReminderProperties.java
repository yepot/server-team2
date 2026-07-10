package com.hackathon.domain.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification.reminder")
public record NotificationReminderProperties(
		int intervalMinutes,
		String instruction
) {
	public NotificationReminderProperties {
		intervalMinutes = intervalMinutes <= 0 ? 5 : intervalMinutes;
		instruction = (instruction == null || instruction.isBlank())
				? "사용자가 행동하도록 재미있고 재촉하는 알림을 작성해 주세요. 알림 횟수가 많을수록 더 단호하게 작성하되 욕설, 모욕, 혐오 표현은 사용하지 마세요."
				: instruction;
	}
}
