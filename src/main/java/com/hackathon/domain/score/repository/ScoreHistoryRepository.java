package com.hackathon.domain.score.repository;

import com.hackathon.domain.score.entity.ScoreActionType;
import com.hackathon.domain.score.entity.ScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoreHistoryRepository extends JpaRepository<ScoreHistory, Long> {

	boolean existsByActionTypeAndBookmark_Id(ScoreActionType actionType, Long bookmarkId);

	boolean existsByActionTypeAndChecklist_Id(ScoreActionType actionType, Long checklistId);
}
