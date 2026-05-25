package com.codelingo.controller;

import com.codelingo.dto.UserProgressResponse;
import com.codelingo.dto.UserStatsResponse;
import com.codelingo.model.User;
import com.codelingo.model.UserProgress;
import com.codelingo.repository.UserProgressRepository;
import com.codelingo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        long completedLevels = userProgressRepository.countByUserAndCompletedTrue(user);

        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "currentStreak", user.getCurrentStreak(),
                "longestStreak", user.getLongestStreak(),
                "totalXp", user.getTotalXp(),
                "completedLevels", completedLevels,
                "createdAt", user.getCreatedAt().toString(),
                "lastActivityDate",
                user.getLastActivityDate() != null
                        ? user.getLastActivityDate().toString()
                        : ""
        ));
    }

    @GetMapping("/me/progress")
    @Transactional(readOnly = true)
    public ResponseEntity<List<UserProgressResponse>> getMyProgress(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<UserProgressResponse> result = userProgressRepository.findByUser(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    private UserProgressResponse toResponse(UserProgress p) {
        UserProgressResponse r = new UserProgressResponse();
        r.setId(p.getId());
        r.setCompleted(p.isCompleted());
        r.setCompletedAt(p.getCompletedAt());
        r.setAttempts(p.getAttempts());
        r.setScore(p.getScore());
        if (p.getLevel() != null) {
            r.setLevelId(p.getLevel().getId());
            r.setLevelNumber(p.getLevel().getLevelNumber());
            r.setLevelTitle(p.getLevel().getTitle());
            r.setXpReward(p.getLevel().getXpReward());
            if (p.getLevel().getLevelGroup() != null) {
                r.setLevelGroupName(p.getLevel().getLevelGroup().getName());
            }
        }
        return r;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<UserStatsResponse>> getAllUsers() {
        List<UserStatsResponse> result = userRepository.findAll()
                .stream()
                .map(u -> {
                    UserStatsResponse r = new UserStatsResponse();
                    r.setId(u.getId());
                    r.setUsername(u.getUsername());
                    r.setEmail(u.getEmail());
                    r.setRole(u.getRole().name());
                    r.setTotalXp(u.getTotalXp());
                    r.setCurrentStreak(u.getCurrentStreak());
                    r.setLongestStreak(u.getLongestStreak());
                    r.setCreatedAt(u.getCreatedAt().toString());
                    r.setLastActivityDate(
                            u.getLastActivityDate() != null ? u.getLastActivityDate().toString() : ""
                    );
                    r.setCompletedLevels(
                            userProgressRepository.countByUserAndCompletedTrue(u)
                    );
                    return r;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}