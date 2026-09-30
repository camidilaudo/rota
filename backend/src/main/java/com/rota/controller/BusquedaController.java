package com.rota.controller;

import com.rota.dto.response.BusquedaProductoLotesResponseDTO;
import com.rota.entity.Ubicacion;
import com.rota.service.BusquedaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/busqueda")
@RequiredArgsConstructor
public class BusquedaController {

    private final BusquedaService busquedaService;

    @GetMapping("/productos")
    public ResponseEntity<Page<BusquedaProductoLotesResponseDTO>> buscarProductos(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Ubicacion ubicacion,
            @PageableDefault(page = 0, size = 10, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {
        
        return ResponseEntity.ok(busquedaService.buscarProductosConLotes(query, ubicacion, pageable));
    }
}