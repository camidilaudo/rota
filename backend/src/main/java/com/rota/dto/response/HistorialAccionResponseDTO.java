package com.rota.dto.response;

import com.rota.entity.TipoAccion;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class HistorialAccionResponseDTO {
    private Long id;
    private Long loteId;
    private TipoAccion tipoAccion;
    private String motivo;
    private String usuarioNombre;
    private String usuarioEmail;
    private LocalDateTime fechaHora;
}
