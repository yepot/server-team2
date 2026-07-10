package com.hackathon.domain.push.entity;

import com.hackathon.domain.member.entity.Member;
import com.hackathon.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "push_subscription")
public class PushSubscription extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Column(nullable = false, unique = true, columnDefinition = "TEXT")
	private String endpoint;

	@Column(name = "p256dh_key", nullable = false, length = 512)
	private String p256dh;

	@Column(name = "auth_key", nullable = false, length = 255)
	private String auth;

	public PushSubscription(Member member, String endpoint, String p256dh, String auth) {
		this.member = member;
		this.endpoint = endpoint;
		this.p256dh = p256dh;
		this.auth = auth;
	}

	public void updateSubscription(Member member, String p256dh, String auth) {
		this.member = member;
		this.p256dh = p256dh;
		this.auth = auth;
	}
}
