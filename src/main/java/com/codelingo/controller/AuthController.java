package com.codelingo.controller;

import com.codelingo.dto.ForgotPasswordRequest;
import com.codelingo.dto.JwtResponse;
import com.codelingo.dto.LoginRequest;
import com.codelingo.dto.RegisterRequest;
import com.codelingo.dto.ResetPasswordRequest;
import com.codelingo.model.User;
import com.codelingo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "Usuario registrado exitosamente. Revisá tu email para verificar tu cuenta.",
                        "userId", user.getId(),
                        "username", user.getUsername()
                ));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(Map.of(
                "message", "Revisá tu bandeja de entrada y seguí las instrucciones, si no ves el correo electrónico, asegurate de revisar tu bandeja de spam o correos no deseados."
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente"));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("message", "Email verificado exitosamente"));
    }
}