package com.hackathon.domain.checklist.service;

import com.hackathon.domain.bookmark.entity.Bookmark;
import com.hackathon.domain.bookmark.repository.BookmarkRepository;
import com.hackathon.domain.checklist.dto.ChecklistDto.ChecklistCheckResponse;
import com.hackathon.domain.checklist.dto.ChecklistDto.ChecklistResponse;
import com.hackathon.domain.checklist.dto.ChecklistDto.CreateRequest;
import com.hackathon.domain.checklist.dto.ChecklistDto.UpdateRequest;
import com.hackathon.domain.checklist.entity.Checklist;
import com.hackathon.domain.checklist.repository.ChecklistRepository;
import com.hackathon.domain.member.entity.Member;
import com.hackathon.domain.score.service.ScoreService;
import com.hackathon.global.exception.CustomException;
import com.hackathon.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChecklistServiceTest {

	@Mock
	private ChecklistRepository checklistRepository;

	@Mock
	private BookmarkRepository bookmarkRepository;

	@Mock
	private ScoreService scoreService;

	@InjectMocks
	private ChecklistService checklistService;

	@Test
	void createChecklistSavesChecklist() {
		Bookmark bookmark = createBookmark(3L, 1L);
		given(bookmarkRepository.findOwnedActiveBookmark(3L, 1L)).willReturn(Optional.of(bookmark));
		given(checklistRepository.saveAndFlush(any(Checklist.class))).willAnswer(invocation -> {
			Checklist checklist = invocation.getArgument(0);
			ReflectionTestUtils.invokeMethod(checklist, "onCreate");
			ReflectionTestUtils.setField(checklist, "id", 15L);
			return checklist;
		});

		ChecklistResponse response =
				checklistService.createChecklist(1L, 3L, new CreateRequest("자기소개서 초안 작성하기"));

		assertThat(response.checklistId()).isEqualTo(15L);
		assertThat(response.bookmarkId()).isEqualTo(3L);
		assertThat(response.content()).isEqualTo("자기소개서 초안 작성하기");
		assertThat(response.isChecked()).isFalse();
		assertThat(response.createdAt()).isNotNull();
	}

	@Test
	void updateChecklistChangesContent() {
		Checklist checklist = createChecklist(15L, 3L, 1L, false);
		given(checklistRepository.findOwnedChecklist(15L, 3L, 1L)).willReturn(Optional.of(checklist));

		ChecklistResponse response =
				checklistService.updateChecklist(1L, 3L, 15L, new UpdateRequest("자기소개서 최종본 작성하기"));

		assertThat(response.content()).isEqualTo("자기소개서 최종본 작성하기");
		assertThat(checklist.getContent()).isEqualTo("자기소개서 최종본 작성하기");
	}

	@Test
	void deleteChecklistRemovesChecklist() {
		Checklist checklist = createChecklist(15L, 3L, 1L, false);
		given(checklistRepository.findOwnedChecklist(15L, 3L, 1L)).willReturn(Optional.of(checklist));

		checklistService.deleteChecklist(1L, 3L, 15L);

		verify(checklistRepository).delete(checklist);
	}

	@Test
	void toggleChecklistChangesCheckedStateAndReturnsUpdatedAt() {
		Checklist checklist = createChecklist(15L, 3L, 1L, false);
		LocalDateTime previousUpdatedAt = LocalDateTime.now().minusMinutes(1);
		ReflectionTestUtils.setField(checklist, "updatedAt", previousUpdatedAt);
		given(checklistRepository.findOwnedChecklist(15L, 3L, 1L)).willReturn(Optional.of(checklist));
		doAnswer(invocation -> {
			ReflectionTestUtils.invokeMethod(checklist, "onUpdate");
			return null;
		}).when(checklistRepository).flush();

		ChecklistCheckResponse response = checklistService.toggleChecklist(1L, 3L, 15L);

		assertThat(response.isChecked()).isTrue();
		assertThat(response.updatedAt()).isAfter(previousUpdatedAt);
		assertThat(checklist.isChecked()).isTrue();
		verify(scoreService).awardChecklistChecked(checklist);
	}

	@Test
	void toggleChecklistDoesNotAwardScoreWhenUnchecking() {
		Checklist checklist = createChecklist(15L, 3L, 1L, true);
		given(checklistRepository.findOwnedChecklist(15L, 3L, 1L)).willReturn(Optional.of(checklist));

		checklistService.toggleChecklist(1L, 3L, 15L);

		verify(scoreService, never()).awardChecklistChecked(checklist);
	}

	@Test
	void createChecklistThrowsWhenBookmarkDoesNotBelongToMember() {
		given(bookmarkRepository.findOwnedActiveBookmark(3L, 1L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> checklistService.createChecklist(1L, 3L, new CreateRequest("내용")))
				.isInstanceOfSatisfying(CustomException.class,
						exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BOOKMARK_NOT_FOUND));
	}

	private Bookmark createBookmark(Long bookmarkId, Long memberId) {
		Member member = Member.builder()
				.loginId("yepot")
				.password("encoded-password")
				.nickname("은서")
				.totalScore(0)
				.build();
		ReflectionTestUtils.setField(member, "id", memberId);

		Bookmark bookmark = Bookmark.builder()
				.memberId(member)
				.url("https://example.com")
				.title("예시 북마크")
				.status("ACTIVE")
				.remindAt(null)
				.build();
		ReflectionTestUtils.setField(bookmark, "id", bookmarkId);

		return bookmark;
	}

	private Checklist createChecklist(Long checklistId, Long bookmarkId, Long memberId, boolean checked) {
		Bookmark bookmark = createBookmark(bookmarkId, memberId);
		Checklist checklist = new Checklist(bookmark, "자기소개서 초안 작성하기");
		ReflectionTestUtils.invokeMethod(checklist, "onCreate");
		ReflectionTestUtils.setField(checklist, "id", checklistId);
		ReflectionTestUtils.setField(checklist, "checked", checked);
		return checklist;
	}
}
