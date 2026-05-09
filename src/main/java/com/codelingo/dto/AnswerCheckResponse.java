package com.codelingo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AnswerCheckResponse {
    private boolean correct;
    private int xpEarned;
    private int attempts;
    private String message;
}