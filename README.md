# High Score Service

Spring Boot service that records player scores in PostgreSQL and maintains leaderboards in Redis.  
In a production setup this would typically use **Kafka** for the score-change event stream; this sample uses **Redis Streams** to simulate the same pattern locally.

## Architecture

- **REST API** (Spring MVC) for score submission and leaderboard reads.
- **PostgreSQL** stores the canonical, idempotent high score per user/game/level.
- **Kafka topic** (conceptual) carries score-change events (in this codebase implemented with a Redis Stream `score-events`).
- **Redis Sorted Sets** hold global and friends leaderboards (eventual consistency).
  - Global key: `leaderboard:global:{gameId}:{levelId}`
  - Friends key: `leaderboard:friends:{userId}:{gameId}:{levelId}`

## High-level design (HLD)

```text
        ┌──────────────────────┐
        │      React UI        │
        │  (index.html/JS)     │
        └─────────┬────────────┘
                  │  HTTP (JSON)
                  ▼
        ┌──────────────────────┐
        │   Spring Boot API    │
        │  ScoreController     │
        └─────────┬────────────┘
                  │  JPA (sync)
                  ▼
        ┌──────────────────────┐
        │     PostgreSQL       │
        │  scores table        │
        └─────────┬────────────┘
                  │  on high-score change
                  │  produce event
                  ▼
        ┌──────────────────────┐
        │   Kafka topic        │  (conceptual)
        │  "score-events"      │  (implemented as Redis Stream)
        └─────────┬────────────┘
                  │  consume (async)
                  ▼
        ┌──────────────────────┐
        │ Background Worker    │
        │ ScoreEventConsumer   │
        │ LeaderboardUpdater   │
        └─────────┬────────────┘
                  │  Redis ZSET writes
                  ▼
        ┌──────────────────────┐
        │        Redis         │
        │ Global/Friends LB    │
        └─────────┬────────────┘
                  │  HTTP (JSON)
                  ▼
        ┌──────────────────────┐
        │   Leaderboard APIs   │
        │   (no DB reads)      │
        └──────────────────────┘
```

## Flow
1. `POST /api/v1/scores` writes/updates the score row synchronously (idempotent via `requestId`).
2. When the high score improves, a message is appended to the **score event stream**:
   - In HLD: published to a Kafka topic.
   - In this demo: appended to Redis Stream `score-events`.
3. `ScoreEventConsumer` asynchronously consumes events (Kafka consumer group in HLD; Redis consumer group `score-workers` in this demo) and calls `LeaderboardUpdater`.
4. `LeaderboardUpdater` writes to the global ZSET and each relevant friends ZSET; leaderboard reads hit Redis only.

## Modules
- `ScoreController` → validates request, delegates to `ScoreService`.
- `ScoreService` → JPA upsert, idempotency check, emits `ScoreEvent` on change.
- `ScoreEventPublisher` → writes to Redis Stream.
- `ScoreEventConsumer` (scheduled) → reads stream, acks, and updates Redis.
- `LeaderboardService` → reads Redis ZSETs for global/friends boards.
- `FriendService` → abstraction for friend lookups (in-memory stub provided).

## API
- `POST /api/v1/scores`
  ```json
  {
    "userId": "alice",
    "gameId": "g1",
    "levelId": "l1",
    "score": 12345,
    "requestId": "dedupe-uuid"
  }
  ```
  Response: `{ "highScoreUpdated": true|false, "score": 12345 }`

- `GET /api/v1/leaderboards/global?gameId=g1&levelId=l1&limit=10`
- `GET /api/v1/leaderboards/friends?userId=alice&gameId=g1&levelId=l1&limit=10`

## Running locally
1. Start PostgreSQL and Redis (e.g., with Docker).
2. Update `src/main/resources/application.properties` with your DB/Redis credentials.
3. Run: `./mvnw spring-boot:run`

## Notes
- Leaderboards are eventually consistent; DB is the source of truth.
- No database reads occur on leaderboard APIs; they rely entirely on Redis.
- Idempotency: repeated submissions with the same `requestId` are no-ops.
- Replace `InMemoryFriendService` with a real friend lookup integration as needed.

