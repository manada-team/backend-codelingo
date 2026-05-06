package com.codelingo.controller;

import com.codelingo.dto.ExecutionRequest;
import com.codelingo.dto.ExecutionResponse;
import com.codelingo.service.CodeExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExecutionController {

    private final CodeExecutionService executionService;

    @PostMapping("/execute")
    public ResponseEntity<ExecutionResponse> execute(@Valid @RequestBody ExecutionRequest request) {
        ExecutionResponse response = executionService.execute(
                request.getCode(), request.getLanguage(), request.getStdin()
        );
        return ResponseEntity.ok(response);
    }
}