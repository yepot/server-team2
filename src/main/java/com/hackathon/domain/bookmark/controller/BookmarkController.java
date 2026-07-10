package com.hackathon.domain.bookmark.controller;

import com.hackathon.domain.bookmark.dto.BookmarkCreateDto.Request;
import com.hackathon.domain.bookmark.dto.BookmarkCreateDto.Response;
import com.hackathon.domain.bookmark.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
@Tag(name = "Bookmark", description = "북마크 관련 API")
public class BookmarkController {

	private final BookmarkService bookmarkService;

	@PostMapping
	@Operation(summary = "북마크 등록")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<Response> create(
			@AuthenticationPrincipal Long memberId,
			@Valid @RequestBody Request request
	) {
		return ResponseEntity.ok(bookmarkService.create(memberId, request));
	}
}
