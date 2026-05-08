//package com.codelingo.controller;
//
////import com.codelingo.dto.LevelProgressRequest;
//import com.codelingo.model.Level;
//import com.codelingo.model.User;
//import com.codelingo.model.UserProgress;
//import com.codelingo.repository.UserRepository;
//import com.codelingo.service.LevelService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/levels")
//@RequiredArgsConstructor
//public class LevelController {
//
//    private final LevelService levelService;
//    private final UserRepository userRepository;
//
//    @GetMapping
//    public ResponseEntity<List<Level>> getAllLevels() {
//        return ResponseEntity.ok(levelService.getAllLevels());
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<Level> getLevel(@PathVariable Long id) {
//        return ResponseEntity.ok(levelService.getLevelById(id));
//    }
//
//    @PostMapping("/{id}/submit")
//    public ResponseEntity<UserProgress> submitLevel(
//            @PathVariable Long id,
//            @Valid @RequestBody LevelProgressRequest request,
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        User user = userRepository.findByUsername(userDetails.getUsername())
//                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
//
//        UserProgress progress = levelService.submitLevel(user, id, request.getSubmittedCode());
//        return ResponseEntity.ok(progress);
//    }
//}