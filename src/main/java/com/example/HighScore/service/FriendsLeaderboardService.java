package com.example.HighScore.service;

import com.example.HighScore.domain.ScoreRecord;
import com.example.HighScore.repository.ScoreRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Recomputes friends leaderboards when a friendship is created.
 * Only updates leaderboards for game/level combinations that both users have played.
 */
@Service
@RequiredArgsConstructor
public class FriendsLeaderboardService {

    private final ScoreRepository scoreRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional(readOnly = true)
    public void recomputeForFriendship(String userA, String userB) {
        if (userA == null || userB == null || userA.isBlank() || userB.isBlank()) {
            return;
        }

        List<ScoreRecord> scoresA = scoreRepository.findByUserId(userA);
        List<ScoreRecord> scoresB = scoreRepository.findByUserId(userB);

        Map<String, Long> mapA = toKeyScoreMap(scoresA); // key = game:level
        Map<String, Long> mapB = toKeyScoreMap(scoresB);

        Set<String> intersection = new HashSet<>(mapA.keySet());
        intersection.retainAll(mapB.keySet());

        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();

        for (String key : intersection) {
            String[] parts = key.split(":");
            String gameId = parts[0];
            String levelId = parts[1];

            String friendsKeyA = friendsKey(userA, gameId, levelId);
            String friendsKeyB = friendsKey(userB, gameId, levelId);

            // Update A's friends leaderboard with B's score
            zset.add(friendsKeyA, userB, mapB.get(key));
            // Update B's friends leaderboard with A's score
            zset.add(friendsKeyB, userA, mapA.get(key));
            // Include self entries for consistency
            zset.add(friendsKeyA, userA, mapA.get(key));
            zset.add(friendsKeyB, userB, mapB.get(key));
        }
    }

    private Map<String, Long> toKeyScoreMap(List<ScoreRecord> scores) {
        Map<String, Long> map = new HashMap<>();
        for (ScoreRecord s : scores) {
            String key = s.getGameId() + ":" + s.getLevelId();
            map.put(key, s.getScore());
        }
        return map;
    }

    private String friendsKey(String userId, String gameId, String levelId) {
        return String.format("leaderboard:friends:%s:%s:%s", userId, gameId, levelId);
    }
}
