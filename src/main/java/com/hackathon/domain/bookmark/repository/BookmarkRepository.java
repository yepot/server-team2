package com.hackathon.domain.bookmark.repository;

import com.hackathon.domain.bookmark.entity.Bookmark;
import com.hackathon.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

	@Query("""
			select b
			from Bookmark b
			where b.id = :bookmarkId
				and b.memberId.id = :memberId
				and b.isActive = true
			""")
	Optional<Bookmark> findOwnedActiveBookmark(@Param("bookmarkId") Long bookmarkId, @Param("memberId") Long memberId);

	@Query("""
			select b
			from Bookmark b
			where b.isActive = true
				and b.remindAt is not null
				and b.remindAt <= :now
				and (
					(select count(n.id) from Notification n where n.bookmark = b) = 0
					or (select max(n.createdAt) from Notification n where n.bookmark = b) <= :repeatThreshold
				)
			order by b.remindAt asc, b.id asc
			""")
	List<Bookmark> findDueReminderBookmarks(
			@Param("now") LocalDateTime now,
			@Param("repeatThreshold") LocalDateTime repeatThreshold
	);

	void deleteAllByMemberId(Member member);
}
