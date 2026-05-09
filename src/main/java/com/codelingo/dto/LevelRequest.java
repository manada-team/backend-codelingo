package com.codelingo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LevelRequest {

    @NotNull
    private Integer levelNumber;

    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String challengeContent;

    @NotBlank
    private String expectedOutput;

//    @NotBlank
//    private String language;

    private int xpReward = 10;

    @NotNull
    private Long levelGroupId;
}