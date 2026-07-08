package com.codelingo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TestCaseRequest {
    private String input;

    @NotBlank
    private String expectedOutput;

    private boolean hidden;
}