package com.rota.service;

import com.rota.dto.request.UsuarioRequestDTO;
import com.rota.dto.response.UsuarioResponseDTO;

public interface UsuarioService {
    UsuarioResponseDTO crear(UsuarioRequestDTO dto);
}
