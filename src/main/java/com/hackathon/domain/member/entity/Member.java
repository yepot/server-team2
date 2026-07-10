package com.hackathon.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.hackathon.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class Member extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String loginId;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String nickname;

	@Column(nullable = false)
	private int totalScore;


	@Builder
	public Member(String loginId, String password, String nickname, int totalScore) {
		this.loginId = loginId;
		this.password = password;
		this.nickname = nickname;
		this.totalScore = totalScore;
	}

	public void increaseScore(int point) {
		this.totalScore += point;
	}

}
