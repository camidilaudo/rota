package com.rota.controller;

import com.rota.dto.request.LoteRequestDTO;
import com.rota.dto.response.LoteResponseDTO;
import com.rota.entity.Ubicacion;
import com.rota.service.LoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lotes")
@RequiredArgsConstructor
public class LoteController {

    private final LoteService loteService;

    @PostMapping
    public ResponseEntity<LoteResponseDTO> registrarLote(@Valid @RequestBody LoteRequestDTO dto) {
        return new ResponseEntity<>(loteService.registrarLote(dto), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/ubicacion")
    public ResponseEntity<LoteResponseDTO> actualizarUbicacion(
            @PathVariable Long id,
            @RequestParam Ubicacion nuevaUbicacion) {
        return ResponseEntity.ok(loteService.actualizarUbicacion(id, nuevaUbicacion));
    }

    @GetMapping("/fefo/producto/{productoId}")
    public ResponseEntity<List<LoteResponseDTO>> obtenerLotesPorFEFO(@PathVariable Long productoId) {
        return ResponseEntity.ok(loteService.obtenerLotesPorFEFO(productoId));
    }
}