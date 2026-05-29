package com.codelingo.repository;

import com.codelingo.model.Level;
import com.codelingo.model.LevelGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LevelRepository extends JpaRepository<Level, Long> {
    List<Level> findByLevelGroupOrderByLevelNumberAsc(LevelGroup levelGroup);
    List<Level> findAllByOrderByLevelNumberAsc();
    Optional<Level> findByLevelNumber(int levelNumber);

}