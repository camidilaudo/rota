package com.rota.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoRequestDTO {

    @NotBlank(message = "El código de barras es obligatorio")
    private String codigoBarra;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombre;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio de venta debe ser positivo")
    private BigDecimal precioVenta;

    @NotNull(message = "El costo es obligatorio")
    @DecimalMin(value = "0.00", message = "El costo no puede ser negativo")
    private BigDecimal costo;

    @NotNull(message = "La categoría asociada es obligatoria")
    private Long categoriaId;
}