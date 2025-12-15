package com.example.HighScore.service;

import com.example.HighScore.api.dto.ScoreSubmissionRequest;
import com.example.HighScore.api.dto.ScoreSubmissionResponse;
import com.example.HighScore.domain.ScoreRecord;
import com.example.HighScore.redis.ScoreEvent;
import com.example.HighScore.redis.ScoreEventPublisher;
import com.example.HighScore.repository.ScoreRepository;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final ScoreEventPublisher scoreEventPublisher;

    @Transactional
    public ScoreSubmissionResponse submitScore(ScoreSubmissionRequest request) {
        Optional<ScoreRecord> existingOpt = scoreRepository.findByUserIdAndGameIdAndLevelId(
                request.getUserId(), request.getGameId(), request.getLevelId());

        if (existingOpt.isPresent()) {
            ScoreRecord existing = existingOpt.get();
            // Idempotent if same request id seen before.
            if (request.getRequestId().equals(existing.getLastRequestId())) {
                return new ScoreSubmissionResponse(false, existing.getScore());
            }

            boolean improved = request.getScore() > existing.getScore();
            if (improved) {
                existing.setScore(request.getScore());
            }
            existing.setLastRequestId(request.getRequestId());
            existing.setUpdatedAt(Instant.now());
            scoreRepository.save(existing);

            if (improved) {
                publishScoreEvent(request);
                return new ScoreSubmissionResponse(true, existing.getScore());
            }
            return new ScoreSubmissionResponse(false, existing.getScore());
        }

        ScoreRecord record = new ScoreRecord();
        record.setUserId(request.getUserId());
        record.setGameId(request.getGameId());
        record.setLevelId(request.getLevelId());
        record.setScore(request.getScore());
        record.setLastRequestId(request.getRequestId());
        record.setUpdatedAt(Instant.now());
        scoreRepository.save(record);

        publishScoreEvent(request);
        return new ScoreSubmissionResponse(true, record.getScore());
    }

    private void publishScoreEvent(ScoreSubmissionRequest request) {
        ScoreEvent event = new ScoreEvent(
                request.getUserId(),
                request.getGameId(),
                request.getLevelId(),
                request.getScore());
        scoreEventPublisher.publish(event);
    }
}

