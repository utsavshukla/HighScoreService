package com.example.HighScore.redis;

import com.example.HighScore.service.FriendService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LeaderboardUpdater {

    private final RedisTemplate<String, String> redisTemplate;
    private final FriendService friendService;

    public void apply(ScoreEvent event) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();

        String globalKey = globalKey(event.getGameId(), event.getLevelId());
        zset.add(globalKey, event.getUserId(), event.getScore());

        Set<String> friendIds = friendService.getFriendIds(event.getUserId());
        // Include the player in their own friends leaderboard for convenience.
        friendIds.add(event.getUserId());
        for (String friendId : friendIds) {
            String friendKey = friendsKey(friendId, event.getGameId(), event.getLevelId());
            zset.add(friendKey, event.getUserId(), event.getScore());
        }
    }

    private String globalKey(String gameId, String levelId) {
        return String.format("leaderboard:global:%s:%s", gameId, levelId);
    }

    private String friendsKey(String userId, String gameId, String levelId) {
        return String.format("leaderboard:friends:%s:%s:%s", userId, gameId, levelId);
    }
}

