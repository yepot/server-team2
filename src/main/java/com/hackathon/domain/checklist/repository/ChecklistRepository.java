package com.hackathon.domain.checklist.repository;

import com.hackathon.domain.checklist.entity.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChecklistRepository extends JpaRepository<Checklist, Long> {

    @Query("""
			select c
			from Checklist c
			join fetch c.bookmark b
			where c.id = :checklistId
				and b.id = :bookmarkId
				and b.memberId.id = :memberId
				and b.isActive = true
			""")
    Optional<Checklist> findOwnedChecklist(
            @Param("checklistId") Long checklistId,
            @Param("bookmarkId") Long bookmarkId,
            @Param("memberId") Long memberId
    );

    @Query("""
			select c.content
			from Checklist c
			where c.bookmark.id = :bookmarkId
				and c.checked = false
			order by c.createdAt asc, c.id asc
			""")
    List<String> findIncompleteContentsByBookmarkId(@Param("bookmarkId") Long bookmarkId);

    boolean existsByBookmark_IdAndCheckedFalse(Long bookmarkId);

    @Query("select c from Checklist c where c.bookmark.id = :bookmarkId")
    List<Checklist> findByBookmarkId(@Param("bookmarkId") Long bookmarkId);

	List<Checklist> findByBookmark_IdInOrderByIdAsc(List<Long> bookmarkIds);
}
