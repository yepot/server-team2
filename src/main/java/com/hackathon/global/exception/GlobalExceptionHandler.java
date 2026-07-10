package com.hackathon.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<Map<String, String>> handleCustomException(CustomException e) {
		return ResponseEntity
				.status(e.getErrorCode().getStatus())
				.body(Map.of("message", e.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handleException(Exception e) {
		return ResponseEntity
				.internalServerError()
				.body(Map.of("message", "서버 내부 오류: " + e.getMessage()));
	}
}
