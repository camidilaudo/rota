package com.rota.service;

import com.rota.dto.response.ComparacionLineaBaseResponseDTO;
import com.rota.dto.response.ResumenMermaResponseDTO;
import com.rota.entity.CausaMerma;

import java.time.LocalDate;

public interface MermaReporteService {

    // HU-25: merma del período a valor costo, por categoría, causa y mes (filtrable por categoría y causa)
    ResumenMermaResponseDTO obtenerResumen(LocalDate desde, LocalDate hasta, Long categoriaId, CausaMerma causa);

    // HU-26: diferencia en pesos y porcentaje contra la línea base de merma mensual
    ComparacionLineaBaseResponseDTO compararContraLineaBase(LocalDate desde, LocalDate hasta);
}
