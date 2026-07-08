package com.codelingo.repository;

import com.codelingo.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findAllByOrderByProblemNumberAsc();
    Optional<Problem> findByProblemNumber(int problemNumber);
}