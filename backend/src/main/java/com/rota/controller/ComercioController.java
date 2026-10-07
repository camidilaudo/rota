package com.rota.controller;

import com.rota.dto.request.ComercioRequestDTO;
import com.rota.dto.response.ComercioResponseDTO;
import com.rota.service.ComercioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comercios")
@RequiredArgsConstructor
public class ComercioController {

    private final ComercioService comercioService;

    @PostMapping
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<ComercioResponseDTO> crear(@Valid @RequestBody ComercioRequestDTO dto) {
        return new ResponseEntity<>(comercioService.crear(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<ComercioResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(comercioService.obtenerPorId(id));
    }
}
