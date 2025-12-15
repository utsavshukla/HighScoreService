package com.example.HighScore.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaderboardEntry {
    private String userId;
    private long score;
    private long rank;
}

