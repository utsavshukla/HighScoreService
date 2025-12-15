package com.example.HighScore.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreEvent {
    private String userId;
    private String gameId;
    private String levelId;
    private long score;
}

