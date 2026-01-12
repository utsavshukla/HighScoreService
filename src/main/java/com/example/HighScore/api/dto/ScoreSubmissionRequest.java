package com.example.HighScore.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScoreSubmissionRequest {

    @NotBlank
    private String userId;

    @NotBlank
    private String gameId;

    @NotBlank
    private String levelId;

    @Min(0)
    private long score;
}

