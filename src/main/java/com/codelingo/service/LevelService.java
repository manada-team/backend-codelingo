package com.codelingo.service;

import com.codelingo.model.Level;
import com.codelingo.model.User;
import com.codelingo.model.UserProgress;
import com.codelingo.repository.LevelRepository;
import com.codelingo.repository.UserProgressRepository;
import com.codelingo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LevelService {

    private final LevelRepository levelRepository;
    private final UserProgressRepository userProgressRepository;
    private final UserRepository userRepository;
    private final StreakService streakService;

    public List<Level> getAllLevels() {
        return levelRepository.findAllByOrderByLevelNumberAsc();
    }

    public Level getLevelById(Long id) {
        return levelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nivel no encontrado: " + id));
    }

    /**
     * Registra el intento del jugador en un nivel.
     * Por ahora valida comparando el output del codigo con el esperado.
     * Logica de ejecucion real se puede agregar en una iteracion futura.
     */
    @Transactional
    public UserProgress submitLevel(User user, Long levelId, String submittedCode) {
        Level level = getLevelById(levelId);

        UserProgress progress = userProgressRepository
                .findByUserAndLevelId(user, levelId)
                .orElse(UserProgress.builder().user(user).level(level).build());

        progress.setAttempts(progress.getAttempts() + 1);

        boolean isCorrect = validateSubmission(level, submittedCode);

        if (isCorrect && !progress.isCompleted()) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
            progress.setScore(calculateScore(level, progress.getAttempts()));

            // Sumar XP al usuario
            user.setTotalXp(user.getTotalXp() + level.getXpReward());
            userRepository.save(user);

            // Actualizar racha
            streakService.registerActivity(user);
        }

        return userProgressRepository.save(progress);
    }

    private boolean validateSubmission(Level level, String submittedCode) {
        // TODO: integrar un motor de ejecucion de codigo real
        // Por ahora es un placeholder que siempre valida contra expectedOutput
        return submittedCode != null
                && level.getExpectedOutput() != null
                && submittedCode.trim().equals(level.getExpectedOutput().trim());
    }

    private int calculateScore(Level level, int attempts) {
        // Mas intentos = menos score, minimo 10% del XP reward
        int base = level.getXpReward() * 10;
        int penalty = (attempts - 1) * 10;
        return Math.max(base - penalty, level.getXpReward());
    }
}