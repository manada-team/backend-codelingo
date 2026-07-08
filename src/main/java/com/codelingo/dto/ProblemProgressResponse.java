package com.codelingo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProblemProgressResponse {
    private Long id;
    private Long problemId;
    private int problemNumber;
    private String problemTitle;
    private boolean completed;
    private LocalDateTime completedAt;
    private int attempts;
    private int score;
    private int xpReward;
}