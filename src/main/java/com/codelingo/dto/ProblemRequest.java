package com.codelingo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ProblemRequest {

    @NotNull
    private Integer problemNumber;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    private String starterCode;

    @NotBlank
    private String difficulty;

    private int xpReward = 20;

    @NotEmpty
    @Valid
    private List<TestCaseRequest> testCases;
}