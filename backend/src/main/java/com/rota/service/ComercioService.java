package com.rota.service;

import com.rota.dto.request.ComercioRequestDTO;
import com.rota.dto.response.ComercioResponseDTO;

public interface ComercioService {
    ComercioResponseDTO crear(ComercioRequestDTO dto);
    ComercioResponseDTO obtenerPorId(Long id);
}
