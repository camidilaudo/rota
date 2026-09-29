package com.rota.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AplicarDescuentoRequestDTO {

    @NotNull(message = "El ID del lote es obligatorio")
    private Long loteId;

    @NotNull(message = "El porcentaje de descuento es obligatorio")
    @DecimalMin(value = "5.00", message = "El descuento mínimo es del 5%")
    @DecimalMax(value = "90.00", message = "El descuento máximo permitido es del 90%")
    private BigDecimal porcentajeDescuento;
}