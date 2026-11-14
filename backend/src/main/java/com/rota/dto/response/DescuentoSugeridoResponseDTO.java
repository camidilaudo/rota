package com.rota.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DescuentoSugeridoResponseDTO {
    private Long loteId;
    private String productoNombre;
    private Integer cantidad;
    private Integer diasHastaVencimiento;

    private BigDecimal velocidadVentaDiariaActual;
    private BigDecimal velocidadVentaDiariaNecesaria;

    // HU-17: porcentaje que liquidaría el lote antes de vencer, sin considerar el piso de margen
    private BigDecimal porcentajeDescuentoSugerido;

    // HU-19: tope máximo que permite el piso de margen configurado
    private BigDecimal margenMinimoPorcentaje;
    private BigDecimal porcentajeDescuentoMaximoPorMargen;

    // HU-19: recomendación final, ya topeada por el piso de margen
    private BigDecimal porcentajeDescuentoRecomendado;

    // HU-19: true si el descuento necesario para vender a tiempo perfora el piso de margen
    private Boolean descuentoInsuficientePorMargen;

    // HU-18: true si aplicar el descuento recomendado deja al menos el mismo margen total que no rebajar
    // (misma proyección que la comparación de escenarios). Si es false, conviene no rebajar.
    private Boolean convieneAplicar;

    private String mensaje;
}
