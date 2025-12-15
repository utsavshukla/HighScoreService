package com.example.HighScore.controller;

import com.example.HighScore.api.dto.ScoreSubmissionRequest;
import com.example.HighScore.api.dto.ScoreSubmissionResponse;
import com.example.HighScore.service.ScoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/scores")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    @PostMapping
    public ResponseEntity<ScoreSubmissionResponse> submitScore(@RequestBody @Valid ScoreSubmissionRequest request) {
        ScoreSubmissionResponse response = scoreService.submitScore(request);
        return ResponseEntity.ok(response);
    }
}

