package com.example.HighScore.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class InMemoryFriendService implements FriendService {

    private final Map<String, Set<String>> friendsByUser = new HashMap<>();

    @Override
    public Set<String> getFriendIds(String userId) {
        return new HashSet<>(friendsByUser.getOrDefault(userId, Collections.emptySet()));
    }

    @Override
    public synchronized void addFriendship(String userId, String friendId) {
        if (userId == null || friendId == null || userId.isBlank() || friendId.isBlank()) {
            return;
        }
        if (userId.equals(friendId)) {
            return;
        }
        friendsByUser.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        friendsByUser.computeIfAbsent(friendId, k -> new HashSet<>()).add(userId);
    }
}

