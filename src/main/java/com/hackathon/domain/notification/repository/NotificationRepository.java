package com.hackathon.domain.notification.repository;

import com.hackathon.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	@Query("""
			select n
			from Notification n
			join fetch n.bookmark b
			where n.member.id = :memberId
				and b.memberId.id = :memberId
				and b.isActive = true
			order by n.createdAt desc, n.id desc
			""")
	List<Notification> findNotificationHistory(@Param("memberId") Long memberId);

	long countByBookmark_Id(Long bookmarkId);

	Optional<Notification> findTopByBookmark_IdOrderByCreatedAtDescIdDesc(Long bookmarkId);
}
