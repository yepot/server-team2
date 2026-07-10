package com.hackathon.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
	DUPLICATE_USERNAME(HttpStatus.CONFLICT, "이미 가입된 아이디입니다."),
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
	BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 북마크입니다."),
	BOOKMARK_UNAVAILABLE(HttpStatus.NOT_FOUND, "조회할 수 없는 북마크입니다."),
	BOOKMARK_ALREADY_DELETED(HttpStatus.NOT_FOUND, "이미 삭제되었거나 조회할 수 없는 북마크입니다."),
	CHECKLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 체크리스트입니다."),
	OPENAI_API_KEY_MISSING(HttpStatus.INTERNAL_SERVER_ERROR, "OpenAI API Key가 설정되지 않았습니다."),
	NOTIFICATION_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI 알림 생성에 실패했습니다."),
	PUSH_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "Web Push 설정이 완료되지 않았습니다."),
	FORBIDDEN_BOOKMARK_ACCESS(HttpStatus.FORBIDDEN, "해당 북마크를 조회할 권한이 없습니다."),
	FORBIDDEN_BOOKMARK_UPDATE(HttpStatus.FORBIDDEN, "해당 북마크를 수정할 권한이 없습니다."),
	FORBIDDEN_BOOKMARK_DELETE(HttpStatus.FORBIDDEN, "해당 북마크를 삭제할 권한이 없습니다."),
	NOTIFICATION_STREAM_CONNECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "알림 스트림 연결 중 오류가 발생했습니다."),
	INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");

	private final HttpStatus status;
	private final String message;

	ErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}
}
