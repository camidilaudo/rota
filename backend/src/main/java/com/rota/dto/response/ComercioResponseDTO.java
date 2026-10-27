package com.rota.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
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
    private BigDecimal margenMinimoPorcentaje;
    // HU-26
    private BigDecimal lineaBaseMermaMensual;
    private LocalDateTime fechaAlta;
    private String duenoEmail;
    private List<CategoriaResponseDTO> categoriasPiloto;
}
