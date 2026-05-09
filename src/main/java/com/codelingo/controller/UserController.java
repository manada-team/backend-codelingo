package com.codelingo.controller;

import com.codelingo.model.User;
import com.codelingo.model.UserProgress;
import com.codelingo.repository.UserProgressRepository;
import com.codelingo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserProgressRepository userProgressRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "currentStreak", user.getCurrentStreak(),
                "longestStreak", user.getLongestStreak(),
                "totalXp", user.getTotalXp(),
                "lastActivityDate",
                user.getLastActivityDate() != null
                        ? user.getLastActivityDate().toString()
                        : ""
        ));
    }

    @GetMapping("/me/progress")
    public ResponseEntity<List<UserProgress>> getMyProgress(
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<UserProgress> progress = userProgressRepository.findByUser(user);

        return ResponseEntity.ok(progress);
    }
}