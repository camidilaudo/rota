package com.rota.service;

import com.rota.dto.request.AplicarDescuentoRequestDTO;
import com.rota.dto.request.RegistrarMermaRequestDTO;
import com.rota.dto.request.RegistrarVentaRequestDTO;
import com.rota.dto.response.ComparacionEscenariosResponseDTO;
import com.rota.dto.response.DescuentoAplicadoResponseDTO;
import com.rota.dto.response.DescuentoSugeridoResponseDTO;
import com.rota.dto.response.HistorialAccionResponseDTO;
import com.rota.dto.response.MermaResponseDTO;
import com.rota.dto.response.VentaResponseDTO;

import java.util.List;

public interface OperacionStockService {
    DescuentoAplicadoResponseDTO aplicarDescuento(AplicarDescuentoRequestDTO dto);
    MermaResponseDTO registrarMerma(RegistrarMermaRequestDTO dto);
    List<MermaResponseDTO> obtenerHistorialMermas();

    // HU-20: venta de un producto, descontando stock por FEFO
    VentaResponseDTO registrarVenta(RegistrarVentaRequestDTO dto);

    // HU-17 / HU-19: porcentaje de descuento sugerido para liquidar el lote a tiempo, topeado por el piso de margen
    DescuentoSugeridoResponseDTO calcularDescuentoSugerido(Long loteId);

    // HU-18: comparación de margen proyectado entre aplicar el descuento sugerido o no hacer nada
    ComparacionEscenariosResponseDTO compararEscenarios(Long loteId);

    // HU-22: historial de acciones tomadas sobre un lote
    List<HistorialAccionResponseDTO> obtenerHistorialDeLote(Long loteId);
}
