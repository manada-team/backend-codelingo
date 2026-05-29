package com.codelingo.service;

import com.codelingo.dto.AnswerCheckResponse;
import com.codelingo.dto.LevelRequest;
import com.codelingo.dto.LevelResponse;
import com.codelingo.model.Level;
import com.codelingo.model.LevelGroup;
import com.codelingo.model.User;
import com.codelingo.model.UserProgress;
import com.codelingo.repository.LevelGroupRepository;
import com.codelingo.repository.LevelRepository;
import com.codelingo.repository.UserProgressRepository;
import com.codelingo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LevelService {

    private final LevelRepository levelRepository;
    private final LevelGroupRepository levelGroupRepository;
    private final UserProgressRepository userProgressRepository;
    private final UserRepository userRepository;
    private final StreakService streakService;

    public List<LevelResponse> getAllLevels() {
        return levelRepository.findAllByOrderByLevelNumberAsc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public LevelResponse getLevelById(Long id) {
        return toResponse(findLevel(id));
    }

    @Transactional
    public LevelResponse createLevel(LevelRequest request) {
        LevelGroup group = levelGroupRepository.findById(request.getLevelGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado: " + request.getLevelGroupId()));

        Level level = Level.builder()
                .levelNumber(request.getLevelNumber())
                .title(request.getTitle())
                .description(request.getDescription())
                .challengeContent(request.getChallengeContent())
                .expectedOutput(request.getExpectedOutput())
//                .language(request.getLanguage())
                .xpReward(request.getXpReward() > 0 ? request.getXpReward() : 10)
                .levelGroup(group)
                .build();

        return toResponse(levelRepository.save(level));
    }

    @Transactional
    public LevelResponse updateLevel(Long id, LevelRequest request) {
        Level level = findLevel(id);
        LevelGroup group = levelGroupRepository.findById(request.getLevelGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado: " + request.getLevelGroupId()));

        // agregar esto:
        levelRepository.findByLevelNumber(request.getLevelNumber())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "El número de nivel " + request.getLevelNumber() + " ya está en uso"
                    );
                });


        level.setLevelNumber(request.getLevelNumber());
        level.setTitle(request.getTitle());
        level.setDescription(request.getDescription());
        level.setChallengeContent(request.getChallengeContent());
        level.setExpectedOutput(request.getExpectedOutput());
//        level.setLanguage(request.getLanguage());
        level.setXpReward(request.getXpReward() > 0 ? request.getXpReward() : 10);
        level.setLevelGroup(group);

        return toResponse(levelRepository.save(level));
    }

    @Transactional
    public void deleteLevel(Long id) {
        levelRepository.delete(findLevel(id));
    }

    @Transactional
    public AnswerCheckResponse checkAnswer(User user, Long levelId, String playerAnswer) {
        Level level = findLevel(levelId);

        String lang = user.getActiveLanguage() != null ? user.getActiveLanguage() : "python";
        UserProgress progress = userProgressRepository
                .findByUserAndLevelIdAndLanguage(user, levelId, lang)
                .orElse(UserProgress.builder().user(user).level(level).language(lang).build());

        boolean correct = level.getExpectedOutput() != null
                && playerAnswer != null
                && playerAnswer.trim().equals(level.getExpectedOutput().trim());

        int xpEarned = 0;

        if (!progress.isCompleted()) {
            progress.setAttempts(progress.getAttempts() + 1);

            if (correct) {
                progress.setCompleted(true);
                progress.setCompletedAt(LocalDateTime.now());
                progress.setScore(calculateScore(level, progress.getAttempts()));
                user.setTotalXp(user.getTotalXp() + level.getXpReward());
                if (user.getActiveLanguage() != null) {
                    switch (user.getActiveLanguage()) {
                        case "python" -> user.setXpPython(user.getXpPython() + level.getXpReward());
                        case "java"   -> user.setXpJava(user.getXpJava() + level.getXpReward());
                        case "c"      -> user.setXpC(user.getXpC() + level.getXpReward());
                    }
                }
                userRepository.save(user);
                streakService.registerActivity(user);
                xpEarned = level.getXpReward();
            }
        }

        userProgressRepository.save(progress);

        String message = correct
                ? (xpEarned > 0 ? "¡Correcto! Ganaste " + xpEarned + " XP." : "¡Correcto! Nivel ya completado.")
                : "Respuesta incorrecta. Intentá de nuevo.";

        return new AnswerCheckResponse(correct, xpEarned, progress.getAttempts(), message);
    }

    private Level findLevel(Long id) {
        return levelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nivel no encontrado: " + id));
    }

    private int calculateScore(Level level, int attempts) {
        int base = level.getXpReward() * 10;
        int penalty = (attempts - 1) * 10;
        return Math.max(base - penalty, level.getXpReward());
    }

    private LevelResponse toResponse(Level level) {
        LevelResponse r = new LevelResponse();
        r.setId(level.getId());
        r.setLevelNumber(level.getLevelNumber());
        r.setTitle(level.getTitle());
        r.setDescription(level.getDescription());
        r.setChallengeContent(level.getChallengeContent());
        r.setExpectedOutput(level.getExpectedOutput());
        r.setXpReward(level.getXpReward());
        if (level.getLevelGroup() != null) {
            r.setLevelGroupId(level.getLevelGroup().getId());
            r.setLevelGroupName(level.getLevelGroup().getName());
        }
        return r;
    }

}