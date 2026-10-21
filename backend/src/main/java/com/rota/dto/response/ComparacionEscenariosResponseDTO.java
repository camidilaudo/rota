package com.rota.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComparacionEscenariosResponseDTO {
    private Long loteId;
    private String productoNombre;
    private Integer cantidad;
    private Integer diasHastaVencimiento;

    private EscenarioProyectadoDTO escenarioSinDescuento;
    private EscenarioProyectadoDTO escenarioConDescuento;

    // "CON_DESCUENTO" o "SIN_DESCUENTO": el escenario que deja más margen
    private String recomendacion;
    private String mensaje;
}
