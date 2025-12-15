package com.example.HighScore.redis;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScoreEventPublisher {

    public static final String STREAM_KEY = "score-events";

    private final RedisTemplate<String, String> redisTemplate;

    public RecordId publish(ScoreEvent event) {
        StreamOperations<String, String, String> streamOps = redisTemplate.opsForStream();
        MapRecord<String, String, String> record = StreamRecords.newRecord()
                .in(STREAM_KEY)
                .ofMap(Map.of(
                        "userId", event.getUserId(),
                        "gameId", event.getGameId(),
                        "levelId", event.getLevelId(),
                        "score", String.valueOf(event.getScore())
                ));
        return streamOps.add(record);
    }
}

