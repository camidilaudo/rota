package com.rota.controller;

import com.rota.dto.request.ProductoRequestDTO;
import com.rota.dto.response.ProductoResponseDTO;
import com.rota.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoRequestDTO dto) {
        return new ResponseEntity<>(productoService.crear(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(productoService.obtenerTodos());
    }

    @GetMapping("/codigo/{codigoBarra}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorCodigoBarra(@PathVariable String codigoBarra) {
        return ResponseEntity.ok(productoService.obtenerPorCodigoBarra(codigoBarra));
    }
}