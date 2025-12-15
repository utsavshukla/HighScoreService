package com.example.HighScore.api.dto;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FriendListResponse {

    private String userId;
    private Set<String> friends;
}


