package com.codelingo.dto;

import lombok.Data;

@Data
public class LevelResponse {
    private Long id;
    private int levelNumber;
    private String title;
    private String description;
    private String challengeContent;
    private String expectedOutput;
    //    private String language;
    private int xpReward;
    private Long levelGroupId;
    private String levelGroupName;
}