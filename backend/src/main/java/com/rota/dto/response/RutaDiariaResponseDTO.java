package com.rota.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class RutaDiariaResponseDTO {
    private Integer totalLotesAtencion;
    private Integer totalLotesVencidos;
    private BigDecimal valorTotalEnRiesgo;
    private List<LoteRiesgoResponseDTO> lotesPriorizados;
}