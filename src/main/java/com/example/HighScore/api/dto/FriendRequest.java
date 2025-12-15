package com.example.HighScore.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FriendRequest {

    @NotBlank
    private String userId;

    @NotBlank
    private String friendId;
}


