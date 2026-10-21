package com.rota.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegistrarVentaRequestDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;

    @NotNull(message = "La cantidad vendida es obligatoria")
    @Min(value = 1, message = "Debe vender al menos 1 unidad")
    private Integer cantidad;
}
