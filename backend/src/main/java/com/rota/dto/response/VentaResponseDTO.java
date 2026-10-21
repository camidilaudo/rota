package com.rota.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class VentaResponseDTO {
    private Long productoId;
    private String productoNombre;
    private Integer cantidadVendida;
    private List<LoteAfectadoVentaDTO> lotesAfectados;
    private BigDecimal rotacionDiariaActualizada;
    private String mensaje;
}
