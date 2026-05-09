package com.codelingo.dto;

import lombok.Data;

@Data
public class LevelGroupResponse {
    private Long id;
    private String name;
    private String description;
    private String difficulty;
    private int minLevel;
    private int maxLevel;
}