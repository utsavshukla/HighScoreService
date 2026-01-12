package com.example.HighScore.redis;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScoreEventPublisher {

    public static final String STREAM_KEY = "score-events";
    private static final String CONSUMER_GROUP = "score-workers";

    private final RedisTemplate<String, String> redisTemplate;

    public RecordId publish(ScoreEvent event) {
        StreamOperations<String, String, String> streamOps = redisTemplate.opsForStream();
        
        // Ensure consumer group exists when first message is published
        try {
            streamOps.createGroup(STREAM_KEY, ReadOffset.latest(), CONSUMER_GROUP);
            log.debug("Created consumer group {} for stream {}", CONSUMER_GROUP, STREAM_KEY);
        } catch (Exception e) {
            // Group likely exists already, which is fine
            log.debug("Consumer group {} already exists or cannot be created: {}", CONSUMER_GROUP, e.getMessage());
        }
        
        MapRecord<String, String, String> record = StreamRecords.newRecord()
                .in(STREAM_KEY)
                .ofMap(Map.of(
                        "userId", event.getUserId(),
                        "gameId", event.getGameId(),
                        "levelId", event.getLevelId(),
                        "score", String.valueOf(event.getScore())
                ));
        RecordId recordId = streamOps.add(record);
        log.debug("Published score event to stream: userId={}, gameId={}, levelId={}, score={}, recordId={}", 
                event.getUserId(), event.getGameId(), event.getLevelId(), event.getScore(), recordId);
        return recordId;
    }
}

