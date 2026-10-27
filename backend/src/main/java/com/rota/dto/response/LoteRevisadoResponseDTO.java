package com.rota.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** HU-16: constancia de que el lote fue revisado, con usuario y hora. */
@Data
@Builder
public class LoteRevisadoResponseDTO {

    private Long loteId;
    private String productoNombre;
    private LocalDateTime revisadoEn;
    private String revisadoPor;
}
