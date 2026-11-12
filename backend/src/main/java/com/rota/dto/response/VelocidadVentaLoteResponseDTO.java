package com.rota.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Ventas diarias del producto del lote contra la velocidad que hace falta para agotarlo antes de vencer. */
@Data
@Builder
public class VelocidadVentaLoteResponseDTO {
    private Long loteId;
    private Integer cantidad;
    private Integer diasHastaVencimiento;
    private BigDecimal velocidadVentaDiariaActual;
    // null si el lote ya venció o no tiene stock: no hay velocidad que alcanzar
    private BigDecimal velocidadVentaDiariaNecesaria;
    private List<VentaDiaria> ventasPorDia;

    @Data
    @AllArgsConstructor
    public static class VentaDiaria {
        private LocalDate fecha;
        private Integer unidades;
    }
}
