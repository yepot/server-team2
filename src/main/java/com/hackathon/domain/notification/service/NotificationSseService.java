package com.hackathon.domain.notification.service;

import com.hackathon.domain.member.repository.MemberRepository;
import com.hackathon.domain.notification.dto.NotificationDto.NotificationStreamConnectResponse;
import com.hackathon.domain.notification.dto.NotificationDto.RealtimeNotificationResponse;
import com.hackathon.global.exception.CustomException;
import com.hackathon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSseService {

	private static final long DEFAULT_TIMEOUT_MILLIS = 60L * 60L * 1000L;

	private final MemberRepository memberRepository;
	private final Map<Long, Map<String, SseEmitter>> emitters = new ConcurrentHashMap<>();

	public SseEmitter connect(Long memberId) {
		memberRepository.findById(memberId)
				.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MILLIS);
		String emitterId = UUID.randomUUID().toString();
		emitters.computeIfAbsent(memberId, key -> new ConcurrentHashMap<>()).put(emitterId, emitter);

		emitter.onCompletion(() -> removeEmitter(memberId, emitterId));
		emitter.onTimeout(() -> {
			emitter.complete();
			removeEmitter(memberId, emitterId);
		});
		emitter.onError(throwable -> removeEmitter(memberId, emitterId));

		try {
			emitter.send(
					SseEmitter.event()
							.name("connect")
							.data(new NotificationStreamConnectResponse("알림 스트림 연결에 성공했습니다."))
			);
		} catch (IOException exception) {
			removeEmitter(memberId, emitterId);
			emitter.completeWithError(exception);
			throw new CustomException(ErrorCode.NOTIFICATION_STREAM_CONNECTION_FAILED);
		}

		return emitter;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleNotificationCreated(NotificationCreatedEvent event) {
		Map<String, SseEmitter> memberEmitters = emitters.get(event.memberId());
		if (memberEmitters == null || memberEmitters.isEmpty()) {
			return;
		}

		RealtimeNotificationResponse payload = new RealtimeNotificationResponse(
				event.notificationId(),
				event.bookmarkId(),
				event.title(),
				event.message(),
				event.notificationCount(),
				event.createdAt()
		);

		memberEmitters.forEach((emitterId, emitter) -> sendNotification(event.memberId(), emitterId, emitter, payload));
	}

	private void sendNotification(
			Long memberId,
			String emitterId,
			SseEmitter emitter,
			RealtimeNotificationResponse payload
	) {
		try {
			emitter.send(
					SseEmitter.event()
							.id(String.valueOf(payload.notificationId()))
							.name("notification")
							.data(payload)
			);
		} catch (IOException | IllegalStateException exception) {
			log.debug("Removing broken notification emitter. memberId={}, emitterId={}", memberId, emitterId, exception);
			emitter.complete();
			removeEmitter(memberId, emitterId);
		}
	}

	private void removeEmitter(Long memberId, String emitterId) {
		Map<String, SseEmitter> memberEmitters = emitters.get(memberId);
		if (memberEmitters == null) {
			return;
		}

		memberEmitters.remove(emitterId);
		if (memberEmitters.isEmpty()) {
			emitters.remove(memberId);
		}
	}
}
