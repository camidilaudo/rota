package com.rota.dto.response;

import com.rota.entity.Ubicacion;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class LoteResponseDTO {
    private Long id;
    private Long productoId;
    private String productoCodigoBarra;
    private String productoNombre;
    private Integer cantidad;
    private LocalDate fechaVencimiento;
    private LocalDate fechaRecepcion;
    private Ubicacion ubicacion;
    private Integer diasHastaVencimiento;
}