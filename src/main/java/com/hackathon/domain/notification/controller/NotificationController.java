package com.hackathon.domain.notification.controller;

import com.hackathon.domain.notification.dto.NotificationDto.NotificationHistoryResponse;
import com.hackathon.domain.notification.service.NotificationService;
import com.hackathon.global.exception.CustomException;
import com.hackathon.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Tag(name = "Notification", description = "알림 API")
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	@Operation(summary = "알림 내역 조회")
	public ResponseEntity<NotificationHistoryResponse> getNotifications(
			@AuthenticationPrincipal Long memberId
	) {
		return ResponseEntity.ok(notificationService.getNotifications(requireMemberId(memberId)));
	}

	private Long requireMemberId(Long memberId) {
		if (memberId == null) {
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
		return memberId;
	}
}
