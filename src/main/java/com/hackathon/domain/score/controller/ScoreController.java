package com.hackathon.domain.score.controller;

import com.hackathon.domain.score.dto.ScoreDto.ScoreResponse;
import com.hackathon.domain.score.service.ScoreService;
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
@RequestMapping("/api/score")
@Tag(name = "Score", description = "점수 API")
public class ScoreController {

	private final ScoreService scoreService;

	@GetMapping
	@Operation(summary = "점수 조회")
	public ResponseEntity<ScoreResponse> getScore(
			@AuthenticationPrincipal Long memberId
	) {
		return ResponseEntity.ok(scoreService.getScore(requireMemberId(memberId)));
	}

	private Long requireMemberId(Long memberId) {
		if (memberId == null) {
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
		return memberId;
	}
}
