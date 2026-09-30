package com.rota.controller;

import com.rota.dto.request.ProductoRequestDTO;
import com.rota.dto.response.ProductoResponseDTO;
import com.rota.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoRequestDTO dto) {
        return new ResponseEntity<>(productoService.crear(dto), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DUENO', 'REPOSITOR')")
    public ResponseEntity<List<ProductoResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(productoService.obtenerTodos());
    }

    @GetMapping("/codigo/{codigoBarra}")
    @PreAuthorize("hasAnyRole('DUENO', 'REPOSITOR')")
    public ResponseEntity<ProductoResponseDTO> obtenerPorCodigoBarra(@PathVariable String codigoBarra) {
        return ResponseEntity.ok(productoService.obtenerPorCodigoBarra(codigoBarra));
    }
}