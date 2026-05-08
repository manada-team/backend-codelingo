package com.codelingo.repository;

import com.codelingo.model.LevelGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LevelGroupRepository extends JpaRepository<LevelGroup, Long> {
    List<LevelGroup> findAllByOrderByMinLevelAsc();
}