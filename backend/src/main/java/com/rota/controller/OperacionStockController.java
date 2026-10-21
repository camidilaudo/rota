package com.rota.controller;

import com.rota.dto.request.AplicarDescuentoRequestDTO;
import com.rota.dto.request.RegistrarMermaRequestDTO;
import com.rota.dto.request.RegistrarVentaRequestDTO;
import com.rota.dto.response.ComparacionEscenariosResponseDTO;
import com.rota.dto.response.DescuentoAplicadoResponseDTO;
import com.rota.dto.response.DescuentoSugeridoResponseDTO;
import com.rota.dto.response.HistorialAccionResponseDTO;
import com.rota.dto.response.MermaResponseDTO;
import com.rota.dto.response.VentaResponseDTO;
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

    // HU-20: registro de ventas, descuenta stock por FEFO
    @PostMapping("/ventas")
    public ResponseEntity<VentaResponseDTO> registrarVenta(@Valid @RequestBody RegistrarVentaRequestDTO dto) {
        return new ResponseEntity<>(operacionStockService.registrarVenta(dto), HttpStatus.CREATED);
    }

    // HU-17 / HU-19: descuento sugerido para liquidar el lote a tiempo, topeado por el piso de margen
    @GetMapping("/lotes/{loteId}/descuento-sugerido")
    public ResponseEntity<DescuentoSugeridoResponseDTO> obtenerDescuentoSugerido(@PathVariable Long loteId) {
        return ResponseEntity.ok(operacionStockService.calcularDescuentoSugerido(loteId));
    }

    // HU-18: comparación de margen proyectado entre aplicar el descuento sugerido o no hacer nada
    @GetMapping("/lotes/{loteId}/comparar-escenarios")
    public ResponseEntity<ComparacionEscenariosResponseDTO> compararEscenarios(@PathVariable Long loteId) {
        return ResponseEntity.ok(operacionStockService.compararEscenarios(loteId));
    }

    // HU-22: historial de acciones (descuentos, traslados, bajas) tomadas sobre un lote
    @GetMapping("/lotes/{loteId}/historial")
    public ResponseEntity<List<HistorialAccionResponseDTO>> obtenerHistorialDeLote(@PathVariable Long loteId) {
        return ResponseEntity.ok(operacionStockService.obtenerHistorialDeLote(loteId));
    }
}
