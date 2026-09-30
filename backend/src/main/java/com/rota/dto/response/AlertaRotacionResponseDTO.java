package com.rota.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaRotacionResponseDTO {
    private Long productoId;
    private String productoNombre;
    private String productoCodigoBarra;

    // Lote actual expuesto en Góndola
    private Long loteGondolaId;
    private LocalDate fechaVencimientoGondola;
    private Integer diasHastaVencimientoGondola;

    // Lote retenido en Depósito que vence ANTES
    private Long loteDepositoId;
    private LocalDate fechaVencimientoDeposito;
    private Integer diasHastaVencimientoDeposito;

    private Integer diasDiferencia;
    private String recomendacionAccion;
}