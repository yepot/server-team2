package com.hackathon.domain.score.entity;

import com.hackathon.domain.bookmark.entity.Bookmark;
import com.hackathon.domain.checklist.entity.Checklist;
import com.hackathon.domain.member.entity.Member;
import com.hackathon.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "score_history")
public class ScoreHistory extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bookmark_id")
	private Bookmark bookmark;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "checklist_id")
	private Checklist checklist;

	@Enumerated(EnumType.STRING)
	@Column(name = "action_type", nullable = false, length = 50)
	private ScoreActionType actionType;

	@Column(nullable = false)
	private Integer score;

	@Builder
	public ScoreHistory(
			Member member,
			Bookmark bookmark,
			Checklist checklist,
			ScoreActionType actionType,
			Integer score
	) {
		this.member = member;
		this.bookmark = bookmark;
		this.checklist = checklist;
		this.actionType = actionType;
		this.score = score;
	}
}
