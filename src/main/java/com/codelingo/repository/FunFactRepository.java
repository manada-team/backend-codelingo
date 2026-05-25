package com.codelingo.repository;

import com.codelingo.model.FunFact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface FunFactRepository extends JpaRepository<FunFact, Long> {
    @Query(value = "SELECT * FROM fun_facts ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<FunFact> findRandom();
}