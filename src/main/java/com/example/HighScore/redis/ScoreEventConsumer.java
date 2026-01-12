package com.example.HighScore.redis;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
public class ScoreEventConsumer {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    @Lazy
    private LeaderboardUpdater leaderboardUpdater;

    @Value("${highscore.stream.consumer-group:score-workers}")
    private String consumerGroup;

    @Value("${highscore.stream.consumer-name:worker-1}")
    private String consumerName;

    @Value("${highscore.stream.poll-interval-ms:1000}")
    private long pollIntervalMs;

    @PostConstruct
    public void init() {
        // Verify RedisTemplate is injected
        if (redisTemplate == null) {
            log.error("RedisTemplate is null after @PostConstruct! This indicates a dependency injection issue.");
            throw new IllegalStateException("RedisTemplate must be injected before initialization");
        }
        log.debug("ScoreEventConsumer initialized with RedisTemplate: {}", redisTemplate != null);
    }
    
    private void ensureGroupIfNeeded() {
        if (redisTemplate == null) {
            log.warn("RedisTemplate is not yet initialized, skipping group creation");
            return;
        }
        StreamOperations<String, String, String> streamOps = redisTemplate.opsForStream();
        try {
            streamOps.createGroup(ScoreEventPublisher.STREAM_KEY, ReadOffset.latest(), consumerGroup);
            log.info("Created Redis stream group {}", consumerGroup);
        } catch (Exception e) {
            // Group likely exists; avoid noisy failures.
            log.debug("Stream group {} already exists or cannot be created: {}", consumerGroup, e.getMessage());
        }
    }

    @Scheduled(fixedDelayString = "${highscore.stream.poll-interval-ms:1000}")
    @SuppressWarnings("unchecked")
    public void poll() {
        if (redisTemplate == null) {
            log.debug("RedisTemplate not yet initialized, skipping poll");
            return;
        }
        
        // Ensure group exists before polling
        ensureGroupIfNeeded();
        
        StreamOperations<String, String, String> streamOps = redisTemplate.opsForStream();
        List<MapRecord<String, String, String>> records = streamOps.read(
                Consumer.from(consumerGroup, consumerName),
                StreamReadOptions.empty().count(20).block(Duration.ofMillis(pollIntervalMs)),
                StreamOffset.create(ScoreEventPublisher.STREAM_KEY, ReadOffset.lastConsumed()));

        if (records == null || records.isEmpty()) {
            return;
        }

        for (MapRecord<String, String, String> record : records) {
            try {
                ScoreEvent event = toEvent(record.getValue());
                leaderboardUpdater.apply(event);
                streamOps.acknowledge(ScoreEventPublisher.STREAM_KEY, consumerGroup, record.getId());
            } catch (Exception ex) {
                log.error("Failed processing record {}: {}", record.getId(), ex.getMessage(), ex);
            }
        }
    }

    private ScoreEvent toEvent(Map<String, String> map) {
        return new ScoreEvent(
                map.get("userId"),
                map.get("gameId"),
                map.get("levelId"),
                Long.parseLong(map.get("score"))
        );
    }
}

