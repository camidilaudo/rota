package com.rota.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class EscenarioProyectadoDTO {
    private BigDecimal porcentajeDescuento;
    private BigDecimal precioFinal;
    private Integer unidadesEstimadasVendidas;
    private Integer unidadesEstimadasDescartadas;
    private BigDecimal margenGananciaEstimado;
    private BigDecimal perdidaPorMermaEstimada;
    private BigDecimal margenTotalProyectado;
}
