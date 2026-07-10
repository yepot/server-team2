package com.hackathon.domain.bookmark.repository;

import com.hackathon.domain.bookmark.entity.Bookmark;
import com.hackathon.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

	void deleteAllByMemberId(Member member);

}

