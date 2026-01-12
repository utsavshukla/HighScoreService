package com.example.HighScore.domain;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "scores", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_game_level", columnNames = {"user_id", "game_id", "level_id"})
})
public class ScoreRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "game_id", nullable = false)
    private String gameId;

    @Column(name = "level_id", nullable = false)
    private String levelId;

    @Column(name = "score_value", nullable = false)
    private long score;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;
}

