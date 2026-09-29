package com.rota.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DescuentoAplicadoResponseDTO {
    private Long loteId;
    private String productoNombre;
    private BigDecimal precioOriginal;
    private BigDecimal porcentajeDescuento;
    private BigDecimal precioConDescuento;
    private String mensaje;
}