package com.hackathon.domain.notification.entity;

import com.hackathon.domain.bookmark.entity.Bookmark;
import com.hackathon.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification")
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "bookmark_id", nullable = false)
	private Bookmark bookmark;

	private String title;

	@Column(nullable = false, length = 250)
	private String message;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Builder
	public Notification(Member member, Bookmark bookmark, String title, String message) {
		this.member = member;
		this.bookmark = bookmark;
		this.title = title;
		this.message = message;
	}

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}
}
