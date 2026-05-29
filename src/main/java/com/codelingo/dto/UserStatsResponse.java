package com.codelingo.dto;

import lombok.Data;

@Data
public class UserStatsResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private int totalXp;
    private int xpPython;
    private int xpJava;
    private int xpC;
    private int currentStreak;
    private int longestStreak;
    private long completedLevels;
    private String createdAt;
    private String lastActivityDate;
    private String activeLanguage;
}