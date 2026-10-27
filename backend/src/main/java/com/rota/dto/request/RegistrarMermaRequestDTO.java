package com.rota.dto.request;

import com.rota.entity.CausaMerma;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrarMermaRequestDTO {

    @NotNull(message = "El ID del lote es obligatorio")
    private Long loteId;

    @NotNull(message = "La cantidad a mermar es obligatoria")
    @Min(value = 1, message = "Debe mermar al menos 1 unidad")
    private Integer cantidad;

    // HU-23: la causa es obligatoria (vencimiento, daño o rotura)
    @NotNull(message = "Debe seleccionar la causa de la baja (vencimiento, daño o rotura)")
    private CausaMerma causa;

    // Detalle opcional que acompaña a la causa
    @Size(max = 120, message = "El detalle no puede superar los 120 caracteres")
    private String motivo;
}
