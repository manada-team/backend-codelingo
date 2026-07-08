package com.codelingo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TestCaseResult {
    private int caseNumber;
    private boolean passed;
    private boolean hidden;

    // null cuando hidden = true, para no filtrar el caso oculto al jugador
    private String input;
    private String expectedOutput;
    private String actualOutput;
    private String stderr;

    public static TestCaseResult redacted(int caseNumber, boolean passed) {
        return new TestCaseResult(caseNumber, passed, true, null, null, null, null);
    }
}