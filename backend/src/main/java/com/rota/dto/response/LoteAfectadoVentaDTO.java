package com.rota.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoteAfectadoVentaDTO {
    private Long loteId;
    private Integer cantidadDescontada;
    private Integer cantidadRestanteEnLote;
}
