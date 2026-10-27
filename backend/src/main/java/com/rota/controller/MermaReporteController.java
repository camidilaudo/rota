package com.rota.controller;

import com.rota.dto.response.ComparacionLineaBaseResponseDTO;
import com.rota.dto.response.ResumenMermaResponseDTO;
import com.rota.entity.CausaMerma;
import com.rota.service.MermaReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** HU-25 y HU-26: reportes de merma. Muestran costos, por eso son solo para el dueño (HU-05). */
@RestController
@RequestMapping("/api/operaciones/mermas")
@RequiredArgsConstructor
public class MermaReporteController {

    private final MermaReporteService mermaReporteService;

    // HU-25: sin fechas usa el mes en curso
    @GetMapping("/resumen")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<ResumenMermaResponseDTO> resumen(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) CausaMerma causa) {
        return ResponseEntity.ok(mermaReporteService.obtenerResumen(desde, hasta, categoriaId, causa));
    }

    // HU-26
    @GetMapping("/comparacion-linea-base")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<ComparacionLineaBaseResponseDTO> comparacionLineaBase(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(mermaReporteService.compararContraLineaBase(desde, hasta));
    }
}
