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

	long countByMemberId_IdAndIsActiveTrue(Long memberId);

	@Query("""
			select count(b)
			from Bookmark b
			where b.memberId.id = :memberId
				and b.isActive = true
				and exists (
					select 1
					from Checklist c
					where c.bookmark = b
				)
				and not exists (
					select 1
					from Checklist c
					where c.bookmark = b
						and c.checked = false
				)
			""")
	long countCompletedBookmarksByMemberId(@Param("memberId") Long memberId);

	@Query("""
			select coalesce(sum(b.viewCount), 0)
			from Bookmark b
			where b.memberId.id = :memberId
				and b.isActive = true
			""")
	Integer sumViewCountByMemberId(@Param("memberId") Long memberId);

	void deleteAllByMemberId(Member member);

	@Query("""
			select b
			from Bookmark b
			where b.memberId.id = :memberId
				and b.isActive = true
				and b.remindAt >= :startOfDay
				and b.remindAt < :endOfDay
			order by b.remindAt asc
			""")
	List<Bookmark> findTodayRemindBookmarks(
			@Param("memberId") Long memberId,
			@Param("startOfDay") LocalDateTime startOfDay,
			@Param("endOfDay") LocalDateTime endOfDay
	);

	@Query(value = """
			select * from bookmark b
			where b.member_id = :memberId
				and b.is_active = true
				and b.id not in (:excludeIds)
			order by random()
			limit :limit
			""", nativeQuery = true)
	List<Bookmark> findRandomActiveBookmarksExcluding(
			@Param("memberId") Long memberId,
			@Param("excludeIds") List<Long> excludeIds,
			@Param("limit") int limit
	);

	@Query("""
			select t.name, count(distinct b.id)
			from Bookmark b join b.tags t
			where b.memberId.id = :memberId and b.isActive = true
			group by t.name
			order by t.name asc
			""")
	List<Object[]> countBookmarksByTagName(@Param("memberId") Long memberId);


}
