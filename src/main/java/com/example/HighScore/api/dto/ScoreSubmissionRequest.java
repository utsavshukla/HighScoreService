package com.example.HighScore.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    /**
     * Caller-supplied unique request identifier for idempotency.
     */
    @NotNull
    private String requestId;
}

