package com.rota.service;

import com.rota.dto.request.ActualizarLineaBaseMermaRequestDTO;
import com.rota.dto.request.ActualizarMargenMinimoRequestDTO;
import com.rota.dto.request.ComercioRequestDTO;
import com.rota.dto.response.ComercioResponseDTO;

public interface ComercioService {
    ComercioResponseDTO crear(ComercioRequestDTO dto);
    ComercioResponseDTO obtenerPorId(Long id);
    // HU-19: el dueño configura el piso de margen que protege sus descuentos
    ComercioResponseDTO actualizarMargenMinimo(Long id, ActualizarMargenMinimoRequestDTO dto);

    // HU-26: el dueño carga la merma mensual que tenía antes de usar Rotá
    ComercioResponseDTO actualizarLineaBaseMerma(Long id, ActualizarLineaBaseMermaRequestDTO dto);
}
