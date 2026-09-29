package com.rota.controller;

import com.rota.dto.response.LoteRiesgoResponseDTO;
import com.rota.dto.response.RutaDiariaResponseDTO;
import com.rota.service.MotorRiesgoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/riesgo")
@RequiredArgsConstructor
public class MotorRiesgoController {

    private final MotorRiesgoService motorRiesgoService;

    @GetMapping("/evaluacion-general")
    public ResponseEntity<List<LoteRiesgoResponseDTO>> obtenerEvaluacionGeneral() {
        return ResponseEntity.ok(motorRiesgoService.evaluarTodosLosLotes());
    }

    @GetMapping("/ruta-diaria")
    public ResponseEntity<RutaDiariaResponseDTO> obtenerRutaDiaria() {
        return ResponseEntity.ok(motorRiesgoService.obtenerRutaDiariaPriorizada());
    }
}