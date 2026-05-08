package com.codelingo.service;

import com.codelingo.model.User;
import com.codelingo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StreakService {

    private final UserRepository userRepository;

    /**
     * Registra actividad del usuario y actualiza la racha.
     * - Si jugo ayer: incrementa racha
     * - Si jugo hoy: no cambia
     * - Si paso mas de un dia: reinicia racha a 1
     */
    public void registerActivity(User user) {
        LocalDate today = LocalDate.now();
        LocalDate lastActivity = user.getLastActivityDate();

        if (lastActivity == null || lastActivity.isBefore(today.minusDays(1))) {
            // Racha reiniciada
            user.setCurrentStreak(1);
        } else if (lastActivity.isEqual(today.minusDays(1))) {
            // Dia consecutivo: incrementa racha
            user.setCurrentStreak(user.getCurrentStreak() + 1);
        }
        // Si lastActivity == today, no hace nada

        user.setLastActivityDate(today);

        if (user.getCurrentStreak() > user.getLongestStreak()) {
            user.setLongestStreak(user.getCurrentStreak());
        }

        userRepository.save(user);
    }
}