package com.hackathon.domain.member.entity;

import com.hackathon.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class Member extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;



	@Column(name = "login_id", nullable = false, unique = true)
	private String loginId;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String nickname;

	@Column(name = "total_score", nullable = false)
	private Integer totalScore;

	@Builder
	public Member(String loginId, String password, String nickname, Integer totalScore) {
		this.loginId = loginId;
		this.password = password;
		this.nickname = nickname;
		this.totalScore = totalScore;
	}

	public void addScore(int score) {
		this.totalScore += score;
	}
}
