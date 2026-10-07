package com.rota.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ComercioResponseDTO {
    private Long id;
    private String nombre;
    private String cuit;
    private String direccion;
    private Boolean activo;
    private LocalDateTime fechaAlta;
    private String duenoEmail;
    private List<CategoriaResponseDTO> categoriasPiloto;
}
