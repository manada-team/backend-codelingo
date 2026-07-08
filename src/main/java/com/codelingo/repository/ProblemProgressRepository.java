package com.codelingo.repository;

import com.codelingo.model.ProblemProgress;
import com.codelingo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProblemProgressRepository extends JpaRepository<ProblemProgress, Long> {
    List<ProblemProgress> findByUser(User user);
    Optional<ProblemProgress> findByUserAndProblemIdAndLanguage(User user, Long problemId, String language);
    long countByUserAndCompletedTrue(User user);
}