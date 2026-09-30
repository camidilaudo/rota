package com.rota.service;

import com.rota.dto.request.TrasladoLoteRequestDTO;
import com.rota.dto.response.AlertaRotacionResponseDTO;
import com.rota.dto.response.LoteResponseDTO;

import java.util.List;

public interface RotacionService {
    List<LoteResponseDTO> obtenerLotesPorFefo(Long productoId);
    LoteResponseDTO obtenerProximoLoteAReponer(Long productoId);
    List<AlertaRotacionResponseDTO> evaluarAlertasRotacionIncorrecta();
    LoteResponseDTO trasladarLoteAGondola(TrasladoLoteRequestDTO request);
}