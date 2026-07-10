package com.hackathon.domain.home.controller;

import com.hackathon.domain.home.dto.HomeDto.HomeResponse;
import com.hackathon.domain.home.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
@Tag(name = "Home", description = "홈 화면 API")
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    @Operation(summary = "홈 화면 조회", description = "닉네임, 총점, 오늘의 소화 콘텐츠(최대 3개), 태그별 컬렉션을 조회합니다.")
    public ResponseEntity<HomeResponse> getHome(Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(homeService.getHome(memberId));

    }
}