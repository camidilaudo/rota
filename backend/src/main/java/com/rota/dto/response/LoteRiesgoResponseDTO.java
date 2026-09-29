package com.rota.dto.response;

import com.rota.entity.EstadoRiesgo;
import com.rota.entity.ModoNotificacion;
import com.rota.entity.Ubicacion;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class LoteRiesgoResponseDTO {
    private Long loteId;
    private Long productoId;
    private String productoCodigoBarra;
    private String productoNombre;
    private String categoriaNombre;
    private Integer cantidad;
    private LocalDate fechaVencimiento;
    private Ubicacion ubicacion;
    private Integer diasHastaVencimiento;
    private Integer diasUmbralCritico;
    private EstadoRiesgo estadoRiesgo;
    private ModoNotificacion modoNotificacion;
    private BigDecimal valorEnRiesgo; // Cantidad * Costo
}