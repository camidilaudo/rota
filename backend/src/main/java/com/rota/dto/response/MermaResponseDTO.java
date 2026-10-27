package com.rota.dto.response;

import com.rota.entity.CausaMerma;
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
    // HU-23
    private CausaMerma causa;
    private Long loteId;
    private BigDecimal costoUnitario;
    private BigDecimal perdidaTotal;
    private LocalDateTime fechaRegistro;
}