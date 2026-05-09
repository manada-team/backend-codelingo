package com.codelingo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnswerCheckRequest {
    @NotNull
    private String answer;
}