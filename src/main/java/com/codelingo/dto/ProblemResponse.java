package com.codelingo.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProblemResponse {
    private Long id;
    private int problemNumber;
    private String title;
    private String description;
    private String difficulty;
    private int xpReward;
    private List<TestCaseView> sampleTestCases;
    private int totalTestCases;
}