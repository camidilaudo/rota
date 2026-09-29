package com.rota.service;

import com.rota.dto.response.LoteRiesgoResponseDTO;
import com.rota.dto.response.RutaDiariaResponseDTO;

import java.util.List;

public interface MotorRiesgoService {
    List<LoteRiesgoResponseDTO> evaluarTodosLosLotes();
    RutaDiariaResponseDTO obtenerRutaDiariaPriorizada();
}