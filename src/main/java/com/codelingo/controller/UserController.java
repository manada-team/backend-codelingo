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

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private static final Set<String> VALID_LANGUAGES = Set.of("python", "java", "c");

    private final UserRepository userRepository;
    private final UserProgressRepository userProgressRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        long completedLevels = userProgressRepository.countByUserAndCompletedTrue(user);

        List<String> startedList = parseStartedLanguages(user.getStartedLanguages());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("role", user.getRole().name());
        response.put("currentStreak", user.getCurrentStreak());
        response.put("longestStreak", user.getLongestStreak());
        response.put("totalXp", user.getTotalXp());
        response.put("xpPython", user.getXpPython());
        response.put("xpJava", user.getXpJava());
        response.put("xpC", user.getXpC());
        response.put("activeLanguage", user.getActiveLanguage() != null ? user.getActiveLanguage() : "");
        response.put("startedLanguages", startedList);
        response.put("completedLevels", completedLevels);
        response.put("createdAt", user.getCreatedAt().toString());
        response.put("lastActivityDate", user.getLastActivityDate() != null ? user.getLastActivityDate().toString() : "");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/me/language")
    @Transactional
    public ResponseEntity<?> setActiveLanguage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {

        String lang = body.get("language");
        if (lang == null || !VALID_LANGUAGES.contains(lang)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Lenguaje inválido"));
        }

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<String> started = new ArrayList<>(parseStartedLanguages(user.getStartedLanguages()));

        if (!started.contains(lang)) {
            started.add(lang);
            user.setStartedLanguages(String.join(",", started));
        }

        user.setActiveLanguage(lang);
        userRepository.save(user);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("activeLanguage", lang);
        resp.put("startedLanguages", started);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/me/progress")
    @Transactional(readOnly = true)
    public ResponseEntity<List<UserProgressResponse>> getMyProgress(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<UserProgressResponse> result = userProgressRepository
                .findByUser(user)
                .stream()
                .filter(p -> user.getActiveLanguage() == null
                        || user.getActiveLanguage().isEmpty()
                        || user.getActiveLanguage().equals(p.getLanguage()))
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
                    r.setXpPython(u.getXpPython());
                    r.setXpJava(u.getXpJava());
                    r.setXpC(u.getXpC());
                    r.setCurrentStreak(u.getCurrentStreak());
                    r.setLongestStreak(u.getLongestStreak());
                    r.setActiveLanguage(u.getActiveLanguage() != null ? u.getActiveLanguage() : "");
                    r.setCreatedAt(u.getCreatedAt().toString());
                    r.setLastActivityDate(u.getLastActivityDate() != null ? u.getLastActivityDate().toString() : "");
                    r.setCompletedLevels(userProgressRepository.countByUserAndCompletedTrue(u));
                    return r;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    private List<String> parseStartedLanguages(String raw) {
        if (raw == null || raw.isBlank()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(raw.split(",")));
    }
}