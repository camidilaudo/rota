package com.rota.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ActualizarLineaBaseMermaRequestDTO {

    @NotNull(message = "La línea base de merma mensual es obligatoria")
    @DecimalMin(value = "0.00", message = "La línea base no puede ser negativa")
    @DecimalMax(value = "9999999999.99", message = "La línea base es demasiado alta")
    private BigDecimal lineaBaseMermaMensual;
}
