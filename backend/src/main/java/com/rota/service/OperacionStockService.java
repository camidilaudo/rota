package com.rota.service;

import com.rota.dto.request.AplicarDescuentoRequestDTO;
import com.rota.dto.request.RegistrarMermaRequestDTO;
import com.rota.dto.response.DescuentoAplicadoResponseDTO;
import com.rota.dto.response.MermaResponseDTO;

import java.util.List;

public interface OperacionStockService {
    DescuentoAplicadoResponseDTO aplicarDescuento(AplicarDescuentoRequestDTO dto);
    MermaResponseDTO registrarMerma(RegistrarMermaRequestDTO dto);
    List<MermaResponseDTO> obtenerHistorialMermas();
}