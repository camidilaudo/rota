package com.rota.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ActualizarMargenMinimoRequestDTO {

    @NotNull(message = "El margen mínimo es obligatorio")
    @DecimalMin(value = "0.00", message = "El margen mínimo no puede ser negativo")
    @DecimalMax(value = "95.00", message = "El margen mínimo no puede superar el 95%")
    private BigDecimal margenMinimoPorcentaje;
}
