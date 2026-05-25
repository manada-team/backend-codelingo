package com.codelingo.controller;

import com.codelingo.repository.FunFactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/fun-facts")
@RequiredArgsConstructor
public class FunFactController {
    private final FunFactRepository funFactRepository;

    @GetMapping("/random")
    public ResponseEntity<?> getRandom() {
        return funFactRepository.findRandom()
                .map(f -> ResponseEntity.ok(Map.of("content", f.getContent())))
                .orElse(ResponseEntity.ok(Map.of("content", "")));
    }
}