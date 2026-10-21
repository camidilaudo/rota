package com.rota.service;

import com.rota.dto.response.HistorialAccionResponseDTO;
import com.rota.entity.Lote;
import com.rota.entity.TipoAccion;
import com.rota.entity.Usuario;

import java.util.List;

public interface HistorialAccionService {
    // HU-22: registra quién, qué y por qué actuó sobre un lote
    void registrarAccion(Lote lote, TipoAccion tipoAccion, String motivo, Usuario usuario);

    List<HistorialAccionResponseDTO> obtenerHistorialPorLote(Long loteId);
}
