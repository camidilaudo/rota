package com.rota.controller;

import com.rota.dto.request.CategoriaRequestDTO;
import com.rota.dto.response.CategoriaResponseDTO;
import com.rota.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> crear(@Valid @RequestBody CategoriaRequestDTO dto) {
        return new ResponseEntity<>(categoriaService.crear(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> obtenerTodas() {
        return ResponseEntity.ok(categoriaService.obtenerTodas());
    }

    @PatchMapping("/{id}/configuracion")
    public ResponseEntity<CategoriaResponseDTO> actualizarConfiguracion(
            @PathVariable Long id,
            @RequestParam(required = false) Integer diasUmbral,
            @RequestParam(required = false) String modoNotificacion) {
        return ResponseEntity.ok(categoriaService.actualizarUmbralYNotificacion(id, diasUmbral, modoNotificacion));
    }
}
