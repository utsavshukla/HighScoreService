package com.example.HighScore.redis;

import com.example.HighScore.service.FriendService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaderboardUpdater {

    private final RedisTemplate<String, String> redisTemplate;
    private final FriendService friendService;

    public void apply(ScoreEvent event) {
        try {
            ZSetOperations<String, String> zset = redisTemplate.opsForZSet();

            String globalKey = globalKey(event.getGameId(), event.getLevelId());
            // Explicitly convert score to double for Redis ZSET (Redis uses Double for scores)
            double scoreValue = event.getScore();
            // Add or update the user's score in the global leaderboard
            // Each userId is a separate member in the ZSET, so multiple users can coexist
            Boolean added = zset.add(globalKey, event.getUserId(), scoreValue);
            log.info("Updated global leaderboard: key={}, userId={}, score={}, added={}", 
                    globalKey, event.getUserId(), scoreValue, added);
            
            // Verify the entry was added by checking the ZSET size
            Long size = zset.size(globalKey);
            log.info("Global leaderboard ZSET size after update: key={}, size={}", globalKey, size);
            
            // Also log all current members for debugging
            Set<ZSetOperations.TypedTuple<String>> allMembers = zset.rangeWithScores(globalKey, 0, -1);
            if (allMembers != null && !allMembers.isEmpty()) {
                StringBuilder membersStr = new StringBuilder();
                for (ZSetOperations.TypedTuple<String> member : allMembers) {
                    if (membersStr.length() > 0) {
                        membersStr.append(", ");
                    }
                    membersStr.append(member.getValue()).append(":").append(member.getScore());
                }
                log.info("All members in global leaderboard {}: {}", globalKey, membersStr.toString());
            } else {
                log.info("No members found in global leaderboard {}", globalKey);
            }

            Set<String> friendIds = friendService.getFriendIds(event.getUserId());
            // Include the player in their own friends leaderboard for convenience.
            friendIds.add(event.getUserId());
            for (String friendId : friendIds) {
                String friendKey = friendsKey(friendId, event.getGameId(), event.getLevelId());
                zset.add(friendKey, event.getUserId(), scoreValue);
                log.debug("Updated friends leaderboard: key={}, userId={}, score={}", friendKey, event.getUserId(), scoreValue);
            }
        } catch (Exception e) {
            log.error("Error updating leaderboard for event: userId={}, gameId={}, levelId={}, score={}", 
                    event.getUserId(), event.getGameId(), event.getLevelId(), event.getScore(), e);
            throw e;
        }
    }

    private String globalKey(String gameId, String levelId) {
        return String.format("leaderboard:global:%s:%s", gameId, levelId);
    }

    private String friendsKey(String userId, String gameId, String levelId) {
        return String.format("leaderboard:friends:%s:%s:%s", userId, gameId, levelId);
    }
}

