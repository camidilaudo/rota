package com.rota.dto.response;

import com.rota.entity.Rol;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsuarioResponseDTO {
    private Long id;
    private String nombre;
    private String email;
    private Rol rol;
    private Long comercioId;
}
