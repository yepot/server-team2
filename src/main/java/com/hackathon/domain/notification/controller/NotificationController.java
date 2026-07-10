package com.hackathon.domain.notification.controller;

import com.hackathon.domain.notification.dto.NotificationDto.NotificationGenerationResponse;
import com.hackathon.domain.notification.dto.NotificationDto.NotificationHistoryResponse;
import com.hackathon.domain.notification.service.NotificationProcessingService;
import com.hackathon.domain.notification.service.NotificationSseService;
import com.hackathon.domain.notification.service.NotificationService;
import com.hackathon.global.exception.CustomException;
import com.hackathon.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Tag(name = "Notification", description = "알림 API")
public class NotificationController {

	private final NotificationService notificationService;
	private final NotificationProcessingService notificationProcessingService;
	private final NotificationSseService notificationSseService;

	@GetMapping
	@Operation(summary = "알림 내역 조회")
	public ResponseEntity<NotificationHistoryResponse> getNotifications(
			@AuthenticationPrincipal Long memberId
	) {
		return ResponseEntity.ok(notificationService.getNotifications(requireMemberId(memberId)));
	}

	@PostMapping
	@Operation(summary = "리마인드 알림 생성")
	public ResponseEntity<NotificationGenerationResponse> generateNotifications(
			@AuthenticationPrincipal Long memberId
	) {
		requireMemberId(memberId);
		return ResponseEntity.ok(notificationProcessingService.generateDueNotifications());
	}

	@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	@Operation(summary = "알림 실시간 스트림 연결")
	public ResponseEntity<SseEmitter> streamNotifications(
			@AuthenticationPrincipal Long memberId
	) {
		return ResponseEntity.ok(notificationSseService.connect(requireMemberId(memberId)));
	}

	private Long requireMemberId(Long memberId) {
		if (memberId == null) {
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
		return memberId;
	}
}
