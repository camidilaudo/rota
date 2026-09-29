package com.rota.service;

import com.rota.dto.request.AuthRequestDTO;
import com.rota.dto.response.AuthResponseDTO;

public interface AuthService {
    AuthResponseDTO login(AuthRequestDTO request);
}