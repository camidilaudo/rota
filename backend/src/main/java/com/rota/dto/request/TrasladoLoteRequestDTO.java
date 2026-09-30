package com.rota.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TrasladoLoteRequestDTO {

    @NotNull(message = "El ID del lote de origen es obligatorio")
    private Long loteOrigenId;

    @NotNull(message = "La cantidad a trasladar es obligatoria")
    @Min(value = 1, message = "Debe trasladar al menos 1 unidad")
    private Integer cantidadATrasladar;
}