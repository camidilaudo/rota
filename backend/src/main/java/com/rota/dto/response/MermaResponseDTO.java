package com.rota.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MermaResponseDTO {
    private Long id;
    private Long productoId;
    private String productoNombre;
    private String productoCodigoBarra;
    private Integer cantidadMermada;
    private String motivo;
    private BigDecimal costoUnitario;
    private BigDecimal perdidaTotal;
    private LocalDateTime fechaRegistro;
}