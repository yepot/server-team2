package com.hackathon.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
	DUPLICATE_USERNAME(HttpStatus.CONFLICT, "이미 가입된 아이디입니다."),
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
	BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 북마크입니다."),
	CHECKLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 체크리스트입니다."),
	OPENAI_API_KEY_MISSING(HttpStatus.INTERNAL_SERVER_ERROR, "OpenAI API Key가 설정되지 않았습니다."),
	NOTIFICATION_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI 알림 생성에 실패했습니다."),
	INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");

	private final HttpStatus status;
	private final String message;

	ErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}
}
