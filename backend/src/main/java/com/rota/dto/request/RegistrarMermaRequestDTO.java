package com.rota.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegistrarMermaRequestDTO {

    @NotNull(message = "El ID del lote es obligatorio")
    private Long loteId;

    @NotNull(message = "La cantidad a mermar es obligatoria")
    @Min(value = 1, message = "Debe mermar al menos 1 unidad")
    private Integer cantidad;

    @NotBlank(message = "Debe especificar el motivo de la merma (ej. Vencido, Deteriorado)")
    private String motivo;
}