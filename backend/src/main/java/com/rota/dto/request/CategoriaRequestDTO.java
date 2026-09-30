package com.rota.dto.request;

import com.rota.entity.ModoNotificacion;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoriaRequestDTO {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    private String nombre;

    @NotNull(message = "El umbral de días de alerta es obligatorio")
    @Min(value = 1, message = "El umbral de alerta debe ser de al menos 1 día")
    private Integer diasUmbralAlerta;

    @NotNull(message = "El umbral de días críticos es obligatorio")
    @Min(value = 1, message = "El umbral crítico debe ser de al menos 1 día")
    private Integer diasUmbralCritico;

    private ModoNotificacion modoNotificacion = ModoNotificacion.PANTALLA;

    private Boolean esPiloto = false;
}