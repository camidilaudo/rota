package com.rota.controller;

import com.rota.dto.request.AplicarDescuentoRequestDTO;
import com.rota.dto.request.RegistrarMermaRequestDTO;
import com.rota.dto.response.DescuentoAplicadoResponseDTO;
import com.rota.dto.response.MermaResponseDTO;
import com.rota.service.OperacionStockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operaciones")
@RequiredArgsConstructor
public class OperacionStockController {

    private final OperacionStockService operacionStockService;

    @PostMapping("/descuentos")
    public ResponseEntity<DescuentoAplicadoResponseDTO> aplicarDescuento(@Valid @RequestBody AplicarDescuentoRequestDTO dto) {
        return ResponseEntity.ok(operacionStockService.aplicarDescuento(dto));
    }

    @PostMapping("/mermas")
    public ResponseEntity<MermaResponseDTO> registrarMerma(@Valid @RequestBody RegistrarMermaRequestDTO dto) {
        return new ResponseEntity<>(operacionStockService.registrarMerma(dto), HttpStatus.CREATED);
    }

    @GetMapping("/mermas")
    public ResponseEntity<List<MermaResponseDTO>> obtenerHistorialMermas() {
        return ResponseEntity.ok(operacionStockService.obtenerHistorialMermas());
    }
}