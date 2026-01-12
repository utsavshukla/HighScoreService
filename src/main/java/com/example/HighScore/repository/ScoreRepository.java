package com.example.HighScore.repository;

import com.example.HighScore.domain.ScoreRecord;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoreRepository extends JpaRepository<ScoreRecord, UUID> {

    Optional<ScoreRecord> findByUserIdAndGameIdAndLevelId(String userId, String gameId, String levelId);

    List<ScoreRecord> findByUserId(String userId);
}

