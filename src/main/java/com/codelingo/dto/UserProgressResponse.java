package com.codelingo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProgressResponse {
    private Long id;
    private Long levelId;
    private int levelNumber;
    private String levelTitle;
    private String levelGroupName;
    private boolean completed;
    private LocalDateTime completedAt;
    private int attempts;
    private int score;
    private int xpReward;
}