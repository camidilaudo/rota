package com.rota.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/** HU-26: merma real del período contra la línea base de merma anterior a Rotá. */
@Data
@Builder
public class ComparacionLineaBaseResponseDTO {

    private LocalDate desde;
    private LocalDate hasta;
    private Boolean hayLineaBase;
    private BigDecimal lineaBaseMensual;
    private BigDecimal lineaBasePeriodo;
    private BigDecimal mermaPeriodo;
    private BigDecimal ahorroPesos;
    private BigDecimal ahorroPorcentaje;
    private Boolean mejoro;
    private String mensaje;
}
