package com.hackathon.domain.notification.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.domain.notification.config.NotificationAiProperties;
import com.hackathon.global.exception.CustomException;
import com.hackathon.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OpenAiNotificationContentGenerator implements NotificationContentGenerator {

	private static final int TITLE_MAX_LENGTH = 60;
	private static final int MESSAGE_MAX_LENGTH = 250;

	private static final Map<String, Object> RESPONSE_SCHEMA = Map.of(
			"type", "object",
			"properties", Map.of(
					"title", Map.of(
							"type", "string",
							"minLength", 1,
							"maxLength", TITLE_MAX_LENGTH
					),
					"message", Map.of(
							"type", "string",
							"minLength", 1,
							"maxLength", MESSAGE_MAX_LENGTH
					)
			),
			"required", List.of("title", "message"),
			"additionalProperties", false
	);

	private final NotificationAiProperties properties;
	private final ObjectMapper objectMapper;
	private final RestClient restClient;

	public OpenAiNotificationContentGenerator(
			NotificationAiProperties properties,
			ObjectMapper objectMapper,
			RestClient.Builder restClientBuilder
	) {
		this.properties = properties;
		this.objectMapper = objectMapper;
		this.restClient = restClientBuilder
				.baseUrl(properties.baseUrl())
				.build();
	}

	@Override
	public GeneratedNotificationContent generate(GenerationRequest request) {
		if (!StringUtils.hasText(properties.apiKey())) {
			throw new CustomException(ErrorCode.OPENAI_API_KEY_MISSING);
		}

		ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest(
				properties.model(),
				List.of(
						new ChatMessage("system", buildSystemPrompt()),
						new ChatMessage("user", buildUserPrompt(request))
				),
				new ResponseFormat(
						"json_schema",
						new JsonSchema("notification_content", true, RESPONSE_SCHEMA)
				),
				false
		);

		try {
			ChatCompletionResponse response = restClient.post()
					.uri("/chat/completions")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
					.contentType(MediaType.APPLICATION_JSON)
					.body(chatCompletionRequest)
					.retrieve()
					.body(ChatCompletionResponse.class);

			if (response == null || response.choices() == null || response.choices().isEmpty()) {
				throw new CustomException(ErrorCode.NOTIFICATION_GENERATION_FAILED);
			}

			ChatMessageResponse message = response.choices().get(0).message();
			if (message == null || !StringUtils.hasText(message.content())) {
				log.warn("OpenAI notification response missing content. refusal={}", message != null ? message.refusal() : null);
				throw new CustomException(ErrorCode.NOTIFICATION_GENERATION_FAILED);
			}

			GeneratedNotificationContent generated = objectMapper.readValue(
					message.content(),
					GeneratedNotificationContent.class
			);

			validateGeneratedContent(generated);
			return generated;
		} catch (CustomException exception) {
			throw exception;
		} catch (RestClientException exception) {
			log.error("OpenAI API call failed while generating notification.", exception);
			throw new CustomException(ErrorCode.NOTIFICATION_GENERATION_FAILED);
		} catch (Exception exception) {
			log.error("Failed to parse OpenAI notification response.", exception);
			throw new CustomException(ErrorCode.NOTIFICATION_GENERATION_FAILED);
		}
	}

	private String buildSystemPrompt() {
		return """
				당신은 사용자의 북마크 리마인드 알림을 작성하는 한국어 비서입니다.
				반드시 JSON만 반환하세요.
				title은 짧고 눈에 띄게 작성하고, message는 250자 이내로 작성하세요.
				bookmark 제목과 미완료 체크리스트를 바탕으로 지금 바로 행동하도록 유도하세요.
				재촉 강도는 reminderLevel에 맞추되, 공격적이거나 모욕적이면 안 됩니다.
				""";
	}

	private String buildUserPrompt(GenerationRequest request) {
		String checklistLines = request.incompleteChecklists().stream()
				.map(item -> "- " + item)
				.reduce((left, right) -> left + "\n" + right)
				.orElse("- 없음");

		return """
				아래 정보를 바탕으로 알림 제목과 본문을 생성해 주세요.

				bookmarkTitle: %s
				notificationCount: %d
				reminderLevel: %d
				instruction: %s
				incompleteChecklists:
				%s

				톤 가이드:
				- reminderLevel 1: 가볍고 시작을 유도하는 톤
				- reminderLevel 2: 조금 더 재촉하는 톤
				- reminderLevel 3: 단호하지만 예의 있는 톤
				""".formatted(
				request.bookmarkTitle(),
				request.notificationCount(),
				request.reminderLevel(),
				request.instruction(),
				checklistLines
		);
	}

	private void validateGeneratedContent(GeneratedNotificationContent generated) {
		if (!StringUtils.hasText(generated.title()) || !StringUtils.hasText(generated.message())) {
			throw new CustomException(ErrorCode.NOTIFICATION_GENERATION_FAILED);
		}
		if (generated.title().length() > TITLE_MAX_LENGTH || generated.message().length() > MESSAGE_MAX_LENGTH) {
			throw new CustomException(ErrorCode.NOTIFICATION_GENERATION_FAILED);
		}
	}

	private record ChatCompletionRequest(
			String model,
			List<ChatMessage> messages,
			@JsonProperty("response_format") ResponseFormat responseFormat,
			Boolean store
	) {}

	private record ChatMessage(
			String role,
			String content
	) {}

	private record ResponseFormat(
			String type,
			@JsonProperty("json_schema") JsonSchema jsonSchema
	) {}

	private record JsonSchema(
			String name,
			Boolean strict,
			Map<String, Object> schema
	) {}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record ChatCompletionResponse(
			List<ChatChoice> choices
	) {}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record ChatChoice(
			ChatMessageResponse message
	) {}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record ChatMessageResponse(
			String content,
			String refusal
	) {}
}
