package com.codelingo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RunResponse {
    private boolean allPassed;
    private int passedCount;
    private int totalCount;
    private List<TestCaseResult> results;
}