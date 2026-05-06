package com.codelingo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResponse {

    private String output;
    private String stderr;
    private int exitCode;
    private long timeMs;
    private String error;

    public static ExecutionResponse error(String message) {
        ExecutionResponse r = new ExecutionResponse();
        r.setError(message);
        r.setExitCode(-1);
        return r;
    }

    public static ExecutionResponse timeout() {
        ExecutionResponse r = new ExecutionResponse();
        r.setError("Tiempo de ejecución excedido (10 segundos)");
        r.setExitCode(-1);
        return r;
    }
}