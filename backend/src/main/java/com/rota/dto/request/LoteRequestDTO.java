package com.rota.dto.request;

import com.rota.entity.Ubicacion;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LoteRequestDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser de al menos 1 unidad")
    private Integer cantidad;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    @FutureOrPresent(message = "No se pueden ingresar productos ya vencidos")
    private LocalDate fechaVencimiento;

    @NotNull(message = "La ubicación del lote es obligatoria")
    private Ubicacion ubicacion;
}