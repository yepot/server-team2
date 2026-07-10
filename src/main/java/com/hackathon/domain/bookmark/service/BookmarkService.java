package com.hackathon.domain.bookmark.service;

import com.hackathon.domain.bookmark.dto.BookmarkCreateDto.Request;
import com.hackathon.domain.bookmark.dto.BookmarkCreateDto.Response;
import com.hackathon.domain.bookmark.entity.Bookmark;
import com.hackathon.domain.bookmark.repository.BookmarkRepository;
import com.hackathon.domain.member.entity.Member;
import com.hackathon.domain.member.repository.MemberRepository;
import com.hackathon.global.exception.CustomException;
import com.hackathon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

	private static final int MAX_TAG_COUNT = 5;

	private final BookmarkRepository bookmarkRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public Response create(Long memberId, Request request) {
		validateAuthenticatedMember(memberId);
		validateRequest(request);

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		Bookmark bookmark = Bookmark.builder()
				.memberId(member)
				.title(request.title())
				.url(request.url())
				.remindAt(request.remindAt())
				.build();

		if (request.tags() != null) {
			bookmark.replaceTags(request.tags());
		}

		return Response.from(bookmarkRepository.save(bookmark));
	}

	private void validateAuthenticatedMember(Long memberId) {
		if (memberId == null) {
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
	}

	private void validateRequest(Request request) {
		if (request.tags() != null && request.tags().size() > MAX_TAG_COUNT) {
			throw new IllegalArgumentException("해시태그는 최대 5개까지 등록할 수 있습니다.");
		}
		if (request.remindAt() != null && !request.remindAt().isAfter(LocalDateTime.now())) {
			throw new IllegalArgumentException("리마인드 날짜는 현재 시각 이후로 설정해야 합니다.");
		}
		if (!isValidUrl(request.url())) {
			throw new IllegalArgumentException("올바른 URL 형식이 아닙니다.");
		}
	}

	private boolean isValidUrl(String url) {
		if (url == null || url.isBlank()) {
			return false;
		}
		try {
			URI uri = URI.create(url);
			return uri.getScheme() != null
					&& uri.getHost() != null
					&& ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()));
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
}
