package com.example.HighScore.controller;

import com.example.HighScore.api.dto.LeaderboardResponse;
import com.example.HighScore.service.LeaderboardService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/leaderboards")
@Validated
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/global")
    public ResponseEntity<LeaderboardResponse> global(
            @RequestParam @NotBlank String gameId,
            @RequestParam @NotBlank String levelId,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        return ResponseEntity.ok(leaderboardService.getGlobal(gameId, levelId, limit));
    }

    @GetMapping("/friends")
    public ResponseEntity<LeaderboardResponse> friends(
            @RequestParam @NotBlank String userId,
            @RequestParam @NotBlank String gameId,
            @RequestParam @NotBlank String levelId,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        return ResponseEntity.ok(leaderboardService.getFriends(userId, gameId, levelId, limit));
    }
}

