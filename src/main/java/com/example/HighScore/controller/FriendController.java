package com.example.HighScore.controller;

import com.example.HighScore.api.dto.FriendListResponse;
import com.example.HighScore.api.dto.FriendRequest;
import com.example.HighScore.service.FriendService;
import com.example.HighScore.service.FriendsLeaderboardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/friends")
@Validated
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final FriendsLeaderboardService friendsLeaderboardService;

    @PostMapping
    public ResponseEntity<Void> addFriend(@RequestBody @Valid FriendRequest request) {
        friendService.addFriendship(request.getUserId(), request.getFriendId());
        // Recompute friends leaderboards only for common games/levels
        friendsLeaderboardService.recomputeForFriendship(request.getUserId(), request.getFriendId());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<FriendListResponse> getFriends(@RequestParam @NotBlank String userId) {
        return ResponseEntity.ok(new FriendListResponse(userId, friendService.getFriendIds(userId)));
    }
}


