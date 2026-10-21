package com.rota.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ComercioRequestDTO {

    @NotBlank(message = "El nombre del comercio es obligatorio")
    @Size(max = 100, message = "El nombre del comercio no puede superar los 100 caracteres")
    private String nombre;

    @Size(max = 20, message = "El CUIT no puede superar los 20 caracteres")
    private String cuit;

    @Size(max = 150, message = "La dirección no puede superar los 150 caracteres")
    private String direccion;

    @NotEmpty(message = "Debe seleccionar al menos una categoría piloto")
    private List<Long> categoriasPilotoIds;

    // HU-19: opcional; si no se envía, se usa el valor por defecto del comercio
    @DecimalMin(value = "0.00", message = "El margen mínimo no puede ser negativo")
    @DecimalMax(value = "95.00", message = "El margen mínimo no puede superar el 95%")
    private BigDecimal margenMinimoPorcentaje;
}
