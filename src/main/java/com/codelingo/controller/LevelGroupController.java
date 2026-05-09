package com.codelingo.controller;

import com.codelingo.dto.LevelGroupResponse;
import com.codelingo.model.LevelGroup;
import com.codelingo.repository.LevelGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/level-groups")
@RequiredArgsConstructor
public class LevelGroupController {

    private final LevelGroupRepository levelGroupRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LevelGroupResponse>> getAllLevelGroups() {
        List<LevelGroupResponse> groups = levelGroupRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(groups);
    }

    private LevelGroupResponse toResponse(LevelGroup group) {
        LevelGroupResponse response = new LevelGroupResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setDescription(group.getDescription());
        response.setDifficulty(group.getDifficulty().name());
        response.setMinLevel(group.getMinLevel());
        response.setMaxLevel(group.getMaxLevel());
        return response;
    }
}