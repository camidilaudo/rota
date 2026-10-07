package com.rota.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

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
}
