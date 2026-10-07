package com.rota.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class RutaDiariaResponseDTO {
    private Integer totalLotesAtencion;
    private Integer totalLotesVencidos;

    // HU-05: dato financiero, se omite (null) para el usuario operativo
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal valorTotalEnRiesgo;

    private List<LoteRiesgoResponseDTO> lotesPriorizados;
}