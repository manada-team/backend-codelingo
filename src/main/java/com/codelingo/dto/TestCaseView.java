package com.codelingo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

// Representacion publica de un caso de prueba visible (no oculto)
@Data
@AllArgsConstructor
public class TestCaseView {
    private String input;
    private String expectedOutput;
}