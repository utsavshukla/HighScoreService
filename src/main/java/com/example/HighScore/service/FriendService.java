package com.example.HighScore.service;

import java.util.Set;

public interface FriendService {

    Set<String> getFriendIds(String userId);

    void addFriendship(String userId, String friendId);
}

