package com.codelingo.controller;

import com.codelingo.dto.ProblemAdminResponse;
import com.codelingo.dto.ProblemRequest;
import com.codelingo.dto.ProblemResponse;
import com.codelingo.dto.RunResponse;
import com.codelingo.dto.SubmitRequest;
import com.codelingo.dto.SubmitResponse;
import com.codelingo.model.User;
import com.codelingo.repository.UserRepository;
import com.codelingo.service.ProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<ProblemResponse>> getAllProblems() {
        return ResponseEntity.ok(problemService.getAllProblems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProblemResponse> getProblem(@PathVariable Long id) {
        return ResponseEntity.ok(problemService.getProblemById(id));
    }

    @GetMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProblemAdminResponse> getProblemForAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(problemService.getProblemForAdmin(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProblemResponse> createProblem(@Valid @RequestBody ProblemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(problemService.createProblem(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProblem(@PathVariable Long id, @Valid @RequestBody ProblemRequest request) {
        try {
            return ResponseEntity.ok(problemService.updateProblem(id, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProblem(@PathVariable Long id) {
        problemService.deleteProblem(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/run")
    public ResponseEntity<RunResponse> runSample(@PathVariable Long id, @Valid @RequestBody SubmitRequest request) {
        return ResponseEntity.ok(problemService.runSample(id, request.getCode(), request.getLanguage()));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<SubmitResponse> submit(
            @PathVariable Long id,
            @Valid @RequestBody SubmitRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(problemService.submit(user, id, request.getCode(), request.getLanguage()));
    }
}