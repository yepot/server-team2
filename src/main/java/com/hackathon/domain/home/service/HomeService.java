package com.hackathon.domain.home.service;

import com.hackathon.domain.bookmark.entity.Bookmark;
import com.hackathon.domain.bookmark.entity.BookmarkTag;
import com.hackathon.domain.bookmark.repository.BookmarkRepository;
import com.hackathon.domain.checklist.entity.Checklist;
import com.hackathon.domain.checklist.repository.ChecklistRepository;
import com.hackathon.domain.home.dto.HomeDto.HomeResponse;
import com.hackathon.domain.home.dto.HomeDto.TagCollection;
import com.hackathon.domain.home.dto.HomeDto.TodayArchiveItem;
import com.hackathon.domain.member.entity.Member;
import com.hackathon.domain.member.repository.MemberRepository;
import com.hackathon.global.exception.CustomException;
import com.hackathon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private static final int TODAY_ARCHIVE_SIZE = 3;

    private final MemberRepository memberRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ChecklistRepository checklistRepository;

    public HomeResponse getHome(Long memberId) {
        Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<TodayArchiveItem> todayArchive = pickTodayArchiveBookmarks(memberId).stream()
                .map(this::toTodayArchiveItem)
                .toList();

        List<TagCollection> collections = bookmarkRepository.countBookmarksByTagName(memberId).stream()
                .map(row -> new TagCollection((String) row[0], (Long) row[1]))
                .toList();

        return new HomeResponse(member.getNickname(), member.getTotalScore(), todayArchive, collections);
    }

    // 오늘 리마인드 있는 북마크 우선, 부족하면 랜덤으로 채워서 최대 3개
    private List<Bookmark> pickTodayArchiveBookmarks(Long memberId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<Bookmark> remindToday = bookmarkRepository.findTodayRemindBookmarks(memberId, startOfDay, endOfDay);

        if (remindToday.size() >= TODAY_ARCHIVE_SIZE) {
            return remindToday.subList(0, TODAY_ARCHIVE_SIZE);
        }

        List<Long> excludeIds = remindToday.stream().map(Bookmark::getId).toList();
        int remaining = TODAY_ARCHIVE_SIZE - remindToday.size();

        List<Bookmark> randomFill = bookmarkRepository.findRandomActiveBookmarksExcluding(
                memberId,
                excludeIds.isEmpty() ? List.of(-1L) : excludeIds,
                remaining
        );

        List<Bookmark> result = new ArrayList<>(remindToday);
        result.addAll(randomFill);
        return result;
    }

    private TodayArchiveItem toTodayArchiveItem(Bookmark bookmark) {
        List<Checklist> checklists = checklistRepository.findByBookmarkId(bookmark.getId());
        long checkedCount = checklists.stream().filter(Checklist::isChecked).count();

        List<String> tagNames = bookmark.getTags().stream()
                .map(BookmarkTag::getName)
                .toList();

        return new TodayArchiveItem(
                bookmark.getId(),
                bookmark.getTitle(),
                bookmark.getUrl(),
                tagNames,
                (int) checkedCount,
                checklists.size()
        );
    }
}