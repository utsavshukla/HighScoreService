package com.example.HighScore.service;

import com.example.HighScore.api.dto.LeaderboardEntry;
import com.example.HighScore.api.dto.LeaderboardResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Service
public class LeaderboardService {

    private final RedisTemplate<String, String> redisTemplate;
    private final FriendService friendService;

    public LeaderboardService(RedisTemplate<String, String> redisTemplate, FriendService friendService) {
        this.redisTemplate = redisTemplate;
        this.friendService = friendService;
    }

    public LeaderboardResponse getGlobal(String gameId, String levelId, int limit) {
        String key = globalKey(gameId, levelId);
        List<LeaderboardEntry> entries = mapEntries(key, limit);
        // Log for debugging
        System.out.println("Global leaderboard query: key=" + key + ", entries count=" + entries.size());
        return new LeaderboardResponse("global", gameId, levelId, entries);
    }

    public LeaderboardResponse getFriends(String userId, String gameId, String levelId, int limit) {
        // Read from the pre-computed friends leaderboard key for this user
        String friendsKeyName = friendsKey(userId, gameId, levelId);
        List<LeaderboardEntry> entries = mapEntries(friendsKeyName, limit);
        System.out.println("Friends leaderboard (from Redis) for userId=" + userId + ", gameId=" + gameId + ", levelId=" + levelId +
                ", entries count=" + entries.size());
        return new LeaderboardResponse("friends", gameId, levelId, entries);
    }

    private List<LeaderboardEntry> mapEntries(String key, int limit) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        
        // First, check total size of the ZSET
        Long totalSize = zset.size(key);
        System.out.println("Total entries in Redis ZSET for key " + key + ": " + totalSize);
        
        // Get all entries (up to limit) sorted by score descending
        // reverseRangeWithScores returns entries from highest score to lowest
        // Parameters: (key, start, end) where end=-1 means "all", but we limit to (limit-1) for top N
        Set<ZSetOperations.TypedTuple<String>> tuples = zset.reverseRangeWithScores(key, 0, limit - 1);
        List<LeaderboardEntry> result = new ArrayList<>();
        if (tuples == null || tuples.isEmpty()) {
            System.out.println("No entries found in Redis for key: " + key);
            return result;
        }
        System.out.println("Retrieved " + tuples.size() + " entries from Redis for key: " + key + " (limit was: " + limit + ")");
        long rank = 1;
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            if (tuple.getScore() == null || tuple.getValue() == null) {
                System.out.println("Skipping tuple with null score or value");
                continue;
            }
            String userId = tuple.getValue();
            long score = tuple.getScore().longValue();
            System.out.println("Entry: rank=" + rank + ", userId=" + userId + ", score=" + score);
            result.add(new LeaderboardEntry(userId, score, rank));
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

