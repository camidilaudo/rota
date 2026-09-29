package com.rota.controller;

import com.rota.dto.request.AuthRequestDTO;
import com.rota.dto.response.AuthResponseDTO;
import com.rota.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }
}