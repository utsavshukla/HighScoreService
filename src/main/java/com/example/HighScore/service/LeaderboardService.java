package com.example.HighScore.service;

import com.example.HighScore.api.dto.LeaderboardEntry;
import com.example.HighScore.api.dto.LeaderboardResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Service
public class LeaderboardService {

    private final RedisTemplate<String, String> redisTemplate;

    public LeaderboardService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public LeaderboardResponse getGlobal(String gameId, String levelId, int limit) {
        String key = globalKey(gameId, levelId);
        List<LeaderboardEntry> entries = mapEntries(key, limit);
        return new LeaderboardResponse("global", gameId, levelId, entries);
    }

    public LeaderboardResponse getFriends(String userId, String gameId, String levelId, int limit) {
        String key = friendsKey(userId, gameId, levelId);
        List<LeaderboardEntry> entries = mapEntries(key, limit);
        return new LeaderboardResponse("friends", gameId, levelId, entries);
    }

    private List<LeaderboardEntry> mapEntries(String key, int limit) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> tuples = zset.reverseRangeWithScores(key, 0, limit - 1);
        List<LeaderboardEntry> result = new ArrayList<>();
        if (tuples == null) {
            return result;
        }
        long rank = 1;
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            if (tuple.getScore() == null) {
                continue;
            }
            result.add(new LeaderboardEntry(tuple.getValue(), tuple.getScore().longValue(), rank));
            rank++;
        }
        return result;
    }

    private String globalKey(String gameId, String levelId) {
        return String.format("leaderboard:global:%s:%s", gameId, levelId);
    }

    private String friendsKey(String userId, String gameId, String levelId) {
        return String.format("leaderboard:friends:%s:%s:%s", userId, gameId, levelId);
    }
}

