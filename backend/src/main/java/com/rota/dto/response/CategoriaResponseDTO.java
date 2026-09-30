package com.rota.dto.response;

import com.rota.entity.ModoNotificacion;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoriaResponseDTO {

    private Long id;
    private String nombre;
    private Integer diasUmbralAlerta;
    private Integer diasUmbralCritico;
    private ModoNotificacion modoNotificacion;
    private Boolean esPiloto;
    private Boolean activa;
}