package com.codelingo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SubmitResponse {
    private boolean allPassed;
    private int passedCount;
    private int totalCount;
    private int xpEarned;
    private int attempts;
    private String message;
    private List<TestCaseResult> results;
}