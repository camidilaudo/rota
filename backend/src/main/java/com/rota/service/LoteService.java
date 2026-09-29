package com.rota.service;

import com.rota.dto.request.LoteRequestDTO;
import com.rota.dto.response.LoteResponseDTO;
import com.rota.entity.Ubicacion;

import java.util.List;

public interface LoteService {
    LoteResponseDTO registrarLote(LoteRequestDTO dto);
    LoteResponseDTO actualizarUbicacion(Long loteId, Ubicacion nuevaUbicacion);
    List<LoteResponseDTO> obtenerLotesPorFEFO(Long productoId);
}