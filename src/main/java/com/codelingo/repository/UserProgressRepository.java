package com.codelingo.repository;

import com.codelingo.model.User;
import com.codelingo.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    List<UserProgress> findByUser(User user);
    Optional<UserProgress> findByUserAndLevelId(User user, Long levelId);
    long countByUserAndCompletedTrue(User user);
}