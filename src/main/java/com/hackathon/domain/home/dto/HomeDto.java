package com.hackathon.domain.home.dto;

import java.util.List;

public class HomeDto {

    public record HomeResponse(
            String nickname,
            Integer totalScore,
            List<TodayArchiveItem> todayArchive,
            List<TagCollection> collections
    ) {}

    public record TodayArchiveItem(
            Long bookmarkId,
            String title,
            String url,
            List<String> tags,
            int checkedCount,
            int totalChecklistCount
    ) {}

    public record TagCollection(
            String tagName,
            long bookmarkCount
    ) {}
}