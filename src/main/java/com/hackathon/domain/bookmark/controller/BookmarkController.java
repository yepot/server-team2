package com.hackathon.domain.bookmark.controller;

import com.hackathon.domain.bookmark.dto.BookmarkCreateDto.Request;
import com.hackathon.domain.bookmark.dto.BookmarkCreateDto.Response;
import com.hackathon.domain.bookmark.dto.BookmarkReadDto;
import com.hackathon.domain.bookmark.dto.BookmarkUpdateDto;
import com.hackathon.domain.bookmark.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
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

	@GetMapping
	@Operation(summary = "북마크 조회")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<BookmarkReadDto.Response> findAll(
			@AuthenticationPrincipal Long memberId
	) {
		return ResponseEntity.ok(bookmarkService.findAll(memberId));
	}

	@GetMapping("/{bookmarkId}")
	@Operation(summary = "북마크 상세 조회")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<BookmarkReadDto.DetailResponse> findOne(
			@AuthenticationPrincipal Long memberId,
			@PathVariable Long bookmarkId
	) {
		return ResponseEntity.ok(bookmarkService.findOne(memberId, bookmarkId));
	}

	@PatchMapping("/{bookmarkId}")
	@Operation(summary = "북마크 수정")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<BookmarkUpdateDto.Response> update(
			@AuthenticationPrincipal Long memberId,
			@PathVariable Long bookmarkId,
			@Valid @RequestBody BookmarkUpdateDto.Request request
	) {
		return ResponseEntity.ok(bookmarkService.update(memberId, bookmarkId, request));
	}
}
