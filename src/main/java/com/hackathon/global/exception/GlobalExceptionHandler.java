package com.hackathon.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> handleCustomException(CustomException e, HttpServletRequest request) {
		return ResponseEntity
				.status(e.getErrorCode().getStatus())
				.body(ErrorResponse.of(e.getErrorCode().getStatus(), e.getMessage(), request.getRequestURI()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(
			MethodArgumentNotValidException e,
			HttpServletRequest request
	) {
		String message = e.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "잘못된 요청입니다.")
				.orElse("잘못된 요청입니다.");

		return ResponseEntity
				.badRequest()
				.body(ErrorResponse.of(HttpStatus.BAD_REQUEST, message, request.getRequestURI()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
			IllegalArgumentException e,
			HttpServletRequest request
	) {
		return ResponseEntity
				.badRequest()
				.body(ErrorResponse.of(
						HttpStatus.BAD_REQUEST,
						e.getMessage() != null ? e.getMessage() : "잘못된 요청입니다.",
						request.getRequestURI()
				));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
		return ResponseEntity
				.internalServerError()
				.body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류: " + e.getMessage(), request.getRequestURI()));
	}

	private record ErrorResponse(
			LocalDateTime timestamp,
			int status,
			String error,
			String message,
			String path
	) {
		private static ErrorResponse of(HttpStatus status, String message, String path) {
			return new ErrorResponse(
					LocalDateTime.now(),
					status.value(),
					status.getReasonPhrase(),
					message,
					path
			);
		}
	}
}
