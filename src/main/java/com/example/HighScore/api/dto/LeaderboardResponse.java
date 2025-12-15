package com.example.HighScore.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaderboardResponse {
    private String scope;
    private String gameId;
    private String levelId;
    private List<LeaderboardEntry> entries;
}

