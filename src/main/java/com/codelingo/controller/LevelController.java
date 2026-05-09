package com.codelingo.controller;

import com.codelingo.dto.AnswerCheckRequest;
import com.codelingo.dto.AnswerCheckResponse;
import com.codelingo.dto.LevelRequest;
import com.codelingo.dto.LevelResponse;
import com.codelingo.model.User;
import com.codelingo.repository.UserRepository;
import com.codelingo.service.LevelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/levels")
@RequiredArgsConstructor
public class LevelController {

    private final LevelService levelService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<LevelResponse>> getAllLevels() {
        return ResponseEntity.ok(levelService.getAllLevels());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LevelResponse> getLevel(@PathVariable Long id) {
        return ResponseEntity.ok(levelService.getLevelById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LevelResponse> createLevel(@Valid @RequestBody LevelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(levelService.createLevel(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LevelResponse> updateLevel(@PathVariable Long id, @Valid @RequestBody LevelRequest request) {
        return ResponseEntity.ok(levelService.updateLevel(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLevel(@PathVariable Long id) {
        levelService.deleteLevel(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/check")
    public ResponseEntity<AnswerCheckResponse> checkAnswer(
            @PathVariable Long id,
            @Valid @RequestBody AnswerCheckRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(levelService.checkAnswer(user, id, request.getAnswer()));
    }
}