package com.hackathon.domain.mypage.controller;

import com.hackathon.domain.mypage.dto.MyPageDto.MyPageResponse;
import com.hackathon.domain.mypage.service.MyPageService;
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
@RequestMapping("/api/mypage")
@Tag(name = "MyPage", description = "마이페이지 API")
public class MyPageController {

	private final MyPageService myPageService;

	@GetMapping
	@Operation(summary = "마이페이지 조회")
	public ResponseEntity<MyPageResponse> getMyPage(
			@AuthenticationPrincipal Long memberId
	) {
		return ResponseEntity.ok(myPageService.getMyPage(requireMemberId(memberId)));
	}

	private Long requireMemberId(Long memberId) {
		if (memberId == null) {
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
		return memberId;
	}
}
