package com.example.HighScore.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScoreSubmissionResponse {

    private boolean highScoreUpdated;
    private long score;
}

