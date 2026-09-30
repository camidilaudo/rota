package com.rota.controller;

import com.rota.dto.request.TrasladoLoteRequestDTO;
import com.rota.dto.response.AlertaRotacionResponseDTO;
import com.rota.dto.response.LoteResponseDTO;
import com.rota.service.RotacionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rotacion")
@RequiredArgsConstructor
public class RotacionController {

    private final RotacionService rotacionService;

    @GetMapping("/fefo/producto/{productoId}")
    public ResponseEntity<List<LoteResponseDTO>> obtenerLotesPorFefo(@PathVariable Long productoId) {
        return ResponseEntity.ok(rotacionService.obtenerLotesPorFefo(productoId));
    }

    @GetMapping("/fefo/reponer/{productoId}")
    public ResponseEntity<LoteResponseDTO> obtenerProximoLoteAReponer(@PathVariable Long productoId) {
        return ResponseEntity.ok(rotacionService.obtenerProximoLoteAReponer(productoId));
    }

    @GetMapping("/alertas-rotacion")
    public ResponseEntity<List<AlertaRotacionResponseDTO>> consultarAlertasRotacion() {
        return ResponseEntity.ok(rotacionService.evaluarAlertasRotacionIncorrecta());
    }

    @PostMapping("/trasladar")
    public ResponseEntity<LoteResponseDTO> trasladarAGondola(@Valid @RequestBody TrasladoLoteRequestDTO request) {
        return ResponseEntity.ok(rotacionService.trasladarLoteAGondola(request));
    }
}