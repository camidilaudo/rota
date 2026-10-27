package com.rota.controller;

import com.rota.dto.request.ActualizarLineaBaseMermaRequestDTO;
import com.rota.dto.request.ActualizarMargenMinimoRequestDTO;
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

    // HU-26: el dueño carga la línea base de merma mensual (antes de usar Rotá)
    @PatchMapping("/{id}/linea-base-merma")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<ComercioResponseDTO> actualizarLineaBaseMerma(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarLineaBaseMermaRequestDTO dto) {
        return ResponseEntity.ok(comercioService.actualizarLineaBaseMerma(id, dto));
    }

    // HU-19: el dueño configura el piso de margen mínimo del comercio
    @PatchMapping("/{id}/margen-minimo")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<ComercioResponseDTO> actualizarMargenMinimo(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarMargenMinimoRequestDTO dto) {
        return ResponseEntity.ok(comercioService.actualizarMargenMinimo(id, dto));
    }
}
