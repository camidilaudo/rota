package com.rota.service;

import com.rota.dto.request.CategoriaRequestDTO;
import com.rota.dto.response.CategoriaResponseDTO;

import java.util.List;

public interface CategoriaService {
    CategoriaResponseDTO crear(CategoriaRequestDTO dto);
    CategoriaResponseDTO actualizarUmbralYNotificacion(Long id, Integer diasUmbral, String modoNotificacion);
    List<CategoriaResponseDTO> obtenerTodas();
}