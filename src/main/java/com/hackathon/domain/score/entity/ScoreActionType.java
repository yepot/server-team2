package com.hackathon.domain.score.entity;

public enum ScoreActionType {
	LINK_SAVED(5),
	PURPOSE_SET(5),
	TAG_SET(3),
	REMINDER_SET(5),
	REMINDER_REVISIT(10),
	CHECKLIST_ITEM_COMPLETED(10),
	CHECKLIST_ALL_COMPLETED(30),
	REMINDER_COMPLETED_WITHIN_24H(20),
	RECOMMENDED_BOOKMARK_COMPLETED(15);

	private final int score;

	ScoreActionType(int score) {
		this.score = score;
	}

	public int getScore() {
		return score;
	}
}
